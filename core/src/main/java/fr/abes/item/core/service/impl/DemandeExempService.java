package fr.abes.item.core.service.impl;

import fr.abes.item.core.components.Fichier;
import fr.abes.item.core.components.FichierEnrichiExemp;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.TypeExemp;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeExempDao;
import fr.abes.item.core.repository.item.ILigneFichierExempDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import fr.abes.item.core.service.*;
import fr.abes.item.core.utilitaire.Utilitaires;
import jakarta.persistence.EntityManager;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@ToString
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.EXEMP})
public class DemandeExempService extends DemandeService implements IDemandeService {
    private FichierEnrichiExemp fichierEnrichiExemp;

    private final IDemandeExempDao demandeExempDao;
    private final FileSystemStorageService storageService;
    private final ILigneFichierService ligneFichierService;
    private final ReferenceService referenceService;
    private final JournalService journalService;
    private final TraitementService traitementService;
    private final UtilisateurService utilisateurService;
    private final IZonesAutoriseesDao zonesAutoriseesDao;
    private final ILigneFichierExempDao ligneFichierExempDao;

    @Value("${files.upload.path}")
    private String uploadPath;

    public DemandeExempService(ILibProfileDao libProfileDao, IDemandeExempDao demandeExempDao, FileSystemStorageService storageService, LigneFichierExempService ligneFichierExempService, ReferenceService referenceService, JournalService journalService, TraitementService traitementService, UtilisateurService utilisateurService, IZonesAutoriseesDao zonesAutoriseesDao, ILigneFichierExempDao ligneFichierExempDao, @Qualifier("itemEntityManager") EntityManager entityManager) {
        super(libProfileDao, entityManager);
        this.demandeExempDao = demandeExempDao;
        this.storageService = storageService;
        this.ligneFichierService = ligneFichierExempService;
        this.referenceService = referenceService;
        this.journalService = journalService;
        this.traitementService = traitementService;
        this.utilisateurService = utilisateurService;
        this.zonesAutoriseesDao = zonesAutoriseesDao;
        this.ligneFichierExempDao = ligneFichierExempDao;
    }

    public List<Demande> findAll() {
        List<Demande> liste = new ArrayList<>(demandeExempDao.findAll());
        setIlnShortNameOnList(new ArrayList<>(liste));
        return liste;
    }

    @Override
    public List<DemandeDto> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeDto> ListeDemandeDto = demandeExempDao.getAllActiveDemandesExempForAdmin(iln);
        //TODO 1 chopper les rcr en une iste string, 2 dao xml pour recuperer la liste des libelle avec un tableau mappé, 3 alimenter les entites LIB iteration

        setIlnShortNameOnDemandeDtoList(ListeDemandeDto);
        return ListeDemandeDto;
    }

    @Override
    public List<DemandeDto> getAllActiveDemandesForAdminExtended() {
        List<DemandeDto> ListeDemandeDto = demandeExempDao.getAllActiveDemandesExempForAdminExtended();
        setIlnShortNameOnDemandeDtoList(ListeDemandeDto);
        return ListeDemandeDto;
    }

    public String getLibelleTypeExempDemande(Integer idDemande) {
        return demandeExempDao.getTypeExemp(idDemande).getLibelle();
    }

    @Override
    public Demande save(Demande entity) {
        DemandeExemp demande = (DemandeExemp) entity;
        demande.setDateModification(Calendar.getInstance().getTime());
        DemandeExemp demandeSaved = demandeExempDao.save(demande);
        demandeSaved.setShortname(entity.getShortname());
        return demandeSaved;
    }

    @Override
    public DemandeExemp findById(Integer id) {
        Optional<DemandeExemp> demandeExemp = demandeExempDao.findById(id);
        /*On contrôle si la demande est présente*/
        demandeExemp.ifPresent(this::setIlnShortNameOnDemande);
        return demandeExemp.orElse(null);
    }

    @Override
    public void deleteById(Integer id) {
        //suppression des fichiers et du répertoire
        storageService.changePath(Paths.get(uploadPath + "exemp/" + id));
        storageService.deleteAll();
        demandeExempDao.deleteById(id);
    }

    /**
     * Méthode permettant de chercher les demandeModifs d'un utilisateur
     *
     * @param iln numéro de l'utilisateur propriétaire des demandeModifs
     * @return liste des demandeModifs de l'utilisateur (hors demandeModifs archivées)
     */
    @Override
    public List<DemandeDto> getActiveDemandesForUser(String iln) {
        List<DemandeDto> listeDemandeDto = demandeExempDao.getActiveDemandesExempForUserExceptedPreparedStatus(iln);
        setIlnShortNameOnDemandeDtoList(listeDemandeDto);
        return listeDemandeDto;
    }

    /**
     * mise à jour du type d'exemplarisation en fonction de l'option choisie coté front
     *
     * @param demandeId identifiant de la demande
     * @param typeExempId valeur du type d'exemplarisation
     * @return la demande modifiée
     */
    public Demande majTypeExemp(Integer demandeId, Integer typeExempId) {
        DemandeExemp demandeExemp = this.findById(demandeId);
        TypeExemp typeExemp = referenceService.findTypeExempById(typeExempId);
        if (demandeExemp != null) {
            demandeExemp.setDateModification(Calendar.getInstance().getTime());
            demandeExemp.setTypeExemp(typeExemp);
            demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
            return this.save(demandeExemp);
        }
        return null;
    }

    /**
     * vérification du fichier et création de l'objet correspondant
     *
     * @param file    fichier issu du front
     * @param demande demande concernée
     * @throws IOException              : erreur lecture fichier
     * @throws FileTypeException        : erreur de type de fichier en entrée
     * @throws FileCheckingException    : erreur dans la vérification de l'extension du fichier
     * @throws DemandeCheckingException : erreur dans l'état de la demande
     */
    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.EXEMP); //Retourne un FichierEnrichiExemp
            fichier.generateFileName(demande); //génération nom du fichier
            stockerFichierOnDisk(file, fichier, demandeExemp); //stockage du fichier sur disque, le controle de l'entête du fichier s'effectue ici
            this.majDemandeWithFichierEnrichi(demandeExemp); //mise à jour de la demande avec les paramètres du fichier enrichi : index de recherche, liste des zones, ajout des lignes du fichier dans la BDD
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }
    }

    /**
     * Stockage physique du fichier de la demande sur le disque
     *
     * @param file         fichier à sauvegarder issu du client
     * @param fichier      objet correspondant au fichier
     * @param demandeExemp demande rattachée au fichier
     * @throws IOException : erreur lecture fichier
     * @throws FileCheckingException : erreur vérification fichier
     */
    private void stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeExemp demandeExemp) throws IOException, FileCheckingException {
        Integer numDemande = demandeExemp.getId();
        try {
            storageService.changePath(Paths.get(uploadPath + "exemp/" + numDemande));
            storageService.init();
            storageService.store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + "exemp/" + numDemande));
            //Ici l'objet fichierExemp va etre renseigné avec les zones courante et valeur de ces zones
            fichier.checkFileContent(demandeExemp); //Contrôle de l'entête et contenu du fichier
        } catch (FileCheckingException e) {
            storageService.delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    /**
     * mise à jour de la demande avec les paramètres du fichier enrichi : index de recherche, liste des zones, ajout des lignes du fichier dans la BDD
     *
     * @param demandeExemp demande concernée
     * @throws IOException : erreur lecture fichier
     * @throws DemandeCheckingException : erreur dans la demande
     */
    private void majDemandeWithFichierEnrichi(DemandeExemp demandeExemp) throws IOException, DemandeCheckingException {
        demandeExemp.setIndexRecherche(fichierEnrichiExemp.getIndexRecherche()); //Index de recherche
        demandeExemp.setListeZones(fichierEnrichiExemp.getValeurZones()); //Ligne d'entête sans l'index de recherche
        ligneFichierService.saveFile(storageService.loadAsResource(fichierEnrichiExemp.getFilename()).getFile(), demandeExemp); //Construction des lignes d'exemplaires pour insertion en base sur table LIGNE_FICHIER_EXEMP
        changeState(demandeExemp, Constant.ETATDEM_SIMULATION);
    }

    /**
     * Méthode permettant de générer le fichier initial sur le disque
     *
     * @param demande demande concernant le fichier
     * @throws FileTypeException : état de la demande ou type de demande rendant impossible la fourniture du fichier par la factory
     */
    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getId();
        // préparation du fichier envoyé par l'utilisateur
        fichierEnrichiExemp = (FichierEnrichiExemp) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.EXEMP); //création d'un objet fichier
        fichierEnrichiExemp.generateFileName(demande);  //creation du nom du fichier (fichierenrichi)
        fichierEnrichiExemp.setPath(Paths.get(uploadPath + "exemp/" + numDemande)); //emplacement du dossier ou sera crée le fichier
    }

    /**
     * Méthode permettant de changer l'état d'une demande
     *
     * @param demande     demande sur laquelle appliquer le changement d'état
     * @param etatDemande état vers lequel la demande doit être passée
     * @return demande modifiée
     * @throws DemandeCheckingException si la demande n'est pas dans l'état précédent à celui passé en paramètre
     */
    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        if ((etatDemande == Constant.ETATDEM_ERREUR) || (etatDemande == Constant.ETATDEM_SUPPRIMEE) || (demande.getEtatDemande().getNumEtat() == getPreviousState(etatDemande))) {
            EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            journalService.addEntreeJournal((DemandeExemp) demande, etat);
            return this.save(demande);
        } else {
            throw new DemandeCheckingException(Constant.DEMANDE_IS_NOT_IN_STATE + getPreviousState(etatDemande));
        }
    }

    /**
     * Retourne l'état précédent d'une demande dans le déroulement normal du processus
     *
     * @param etatDemande : etat dont on souhaite connaitre l'état précédent
     * @return : l'état précédent dans le processus
     */
    private int getPreviousState(int etatDemande) {
        return switch (etatDemande) {
            case Constant.ETATDEM_SIMULATION -> Constant.ETATDEM_ACOMPLETER;
            case Constant.ETATDEM_ACOMPLETER -> Constant.ETATDEM_PREPARATION;
            case Constant.ETATDEM_ATTENTE -> Constant.ETATDEM_SIMULATION;
            case Constant.ETATDEM_ENCOURS -> Constant.ETATDEM_ATTENTE;
            case Constant.ETATDEM_TERMINEE -> Constant.ETATDEM_ENCOURS;
            case Constant.ETATDEM_ERREUR -> Constant.ETATDEM_ERREUR;
            case Constant.ETATDEM_ARCHIVEE -> Constant.ETATDEM_TERMINEE;
            case Constant.ETATDEM_SUPPRIMEE -> Constant.ETATDEM_ARCHIVEE;
            // case Constant.ETATDEM_INTEROMPU -> 0; // cas couvert par default
            default -> 0;
        };
    }

    /**
     * Méthode permettant de changer l'état d'une demande vers l'état précédent dans le processus global de l'application
     *
     * @param demande : demande concernée
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un état incorrect
     */
    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException {
        int etatDemande = demande.getEtatDemande().getId();
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        if (etatDemande == Constant.ETATDEM_ACOMPLETER) {
            demandeExemp.setTypeExemp(null);
            demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
            save(demandeExemp);
        } else {
            throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
        return demandeExemp;
    }

    /**
     * Méthode permettant de changer l'état d'une demande vers l'état ciblé dans le processus global de l'application
     * @param etape   etape à laquelle on souhaite retourner
     * @param demande demande que l'on souhaite modifier
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un etat incorrect
     */
    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        switch (etape) {
            case 1 -> demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
            case 2 -> {
                //retour de la demande d'exemplarisation au stade choix du type, à l'état en saisie (=preparation)
                demandeExemp.setTypeExemp(null); //effacement type d'exemplarisation obtenu a : ETAPE2
                demandeExemp.setIndexRecherche(null); //effacement de l'index de recherche obtenu a etape chargement du fichier : ETAPE3
                //le commentaire n'est pas effacé, il est géré dans le tableau de bord : pas dans les ETAPES
                demandeExemp.setListeZones(null); //effacement de la liste des zones obtenu a etape chargement du fichier : ETAPE3
                demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION)); //retour en ETAT 1
                /*suppression des lignes de la table LIGNE_FICHIER_EXEMP crées à chargement du fichier : ETAPE3
                On supprime les lignes qui ont en REF_DEMANDE l'id de la demande*/
                ligneFichierExempDao.deleteLigneFichierExempByDemandeExempId(demandeExemp.getId());
            }
            case 3 -> {
                demandeExemp.setIndexRecherche(null);
                demandeExemp.setListeZones(null);
                demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER)); //retour en ETAT3
                ligneFichierExempDao.deleteLigneFichierExempByDemandeExempId(demandeExemp.getId());
            }
            default -> throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
        return save(demandeExemp);
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return changeState(demande, Constant.ETATDEM_TERMINEE);
    }



    /**
     * Récupération de la prochaine demande en attente à traiter
     *
     * @return demande récupérée dans la base
     */
    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        List<DemandeExemp> listeDemandes;
        if (currentHour >= minHour && currentHour < maxHour) {
            listeDemandes = demandeExempDao.getNextDemandeToProceedWithoutDAT();
        } else {
            listeDemandes = demandeExempDao.getNextDemandeToProceed();
        }
        if (!listeDemandes.isEmpty())
            return listeDemandes.get(0);
        return null;
    }

    /**
     * Récupération de la prochaine demande terminée à archiver
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeExemp> getDemandesToArchive() {
        List<DemandeExemp> listeDemandes = demandeExempDao.getNextDemandeToArchive();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande archivée à placer en statut supprimé
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeExemp> getDemandesToPlaceInDeletedStatus() {
        List<DemandeExemp> listeDemandes = demandeExempDao.getNextDemandeToPlaceInDeletedStatus();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande en statut supprimé à supprimer définitivement
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeExemp> getDemandesToDelete() {
        List<DemandeExemp> listeDemandes = demandeExempDao.getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public Demande restaurerDemande(Demande demande) throws DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        if (demandeExemp.getEtatDemande().getNumEtat() != Constant.ETATDEM_ARCHIVEE)
            throw new DemandeCheckingException("La demande doit être en état archivée !");
        EtatDemande etat = journalService.getDernierEtatConnuAvantArchivage(demandeExemp);
        if (etat != null) {
            demandeExemp.setEtatDemande(etat);
            journalService.addEntreeJournal(demandeExemp,etat);
            return save(demandeExemp);
        }
        return demande;
    }

    @Override
    public DemandeExemp creerDemande(String rcr, Integer userNum) {
        Calendar calendar = Calendar.getInstance();
        DemandeExemp demandeExemp = new DemandeExemp(rcr, calendar.getTime(), calendar.getTime(), referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), null, utilisateurService.findById(userNum));
        demandeExemp.setIln(Objects.requireNonNull(libProfileDao.findById(rcr).orElse(null)).getIln());
        setIlnShortNameOnDemande(demandeExemp);
        DemandeExemp demToReturn = (DemandeExemp) save(demandeExemp);
        journalService.addEntreeJournal(demandeExemp, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION));
        return demToReturn;
    }


    /**
     * Méthode de génération de la première ligne d'en tête du fichier résultat
     *
     * @param demande demande concernée
     * @return la chaine correspondant à l'en tête du fichier de résultat
     */
    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return "Exemplarisation démarrée le;" + Constant.formatDate.format(dateDebut) + "\n"
                + "requête;nb réponses;liste PPN;Correspondance L035;résultat;";
    }

    @Override
    public List<DemandeDto> getAllArchivedDemandes(String iln) {
        List<DemandeDto> listDemandeDto = demandeExempDao.getAllArchivedDemandesExemp(iln);
        setIlnShortNameOnDemandeDtoList(listDemandeDto);
        return listDemandeDto;
    }

    @Override
    public List<DemandeDto> getAllArchivedDemandesAllIln() {
        List<DemandeDto> listeDemandeDto = demandeExempDao.getAllArchivedDemandesExempExtended();
        setIlnShortNameOnDemandeDtoList(listeDemandeDto);
        return listeDemandeDto;
    }

    /**
     * méthode d'archivage d'une demande
     * supprime les lignes fichiers au moment de l'archivage
     *
     * @param demande demande à archiver
     * @return la demande dans l'état archivé
     * @throws DemandeCheckingException : erreur de vérification de la demande
     */
    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        return changeState(demandeExemp, Constant.ETATDEM_ARCHIVEE);
    }

    @Override
    public void modifierShortNameDemande(Demande demande) {
        setIlnShortNameOnDemande(demande);
    }

    @Override
    public void refreshEntity(Demande demande) {
        entityManager.refresh(demande);
    }

    @Override
    public void cleanLignesFichierDemande(Demande demande) {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        ligneFichierService.deleteByDemande(demandeExemp);
    }
}
