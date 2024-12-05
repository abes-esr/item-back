package fr.abes.item.core.service.impl;

import fr.abes.item.core.components.*;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.Traitement;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeModifDao;
import fr.abes.item.core.service.*;
import fr.abes.item.core.utilitaire.Utilitaires;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.MODIF})
public class DemandeModifService extends DemandeService implements IDemandeService {
    private final IDemandeModifDao demandeModifDao;
    private final FileSystemStorageService storageService;
    private final ILigneFichierService ligneFichierService;
    private final JournalService journalService;
    private final ReferenceService referenceService;
    private final UtilisateurService utilisateurService;
    private final Ppntoepn procStockee;
    private final EntityManager entityManager;

    @Value("${files.upload.path}")
    private String uploadPath;

    private FichierInitial fichierInit;
    private FichierPrepare fichierPrepare;

    public DemandeModifService(ILibProfileDao libProfileDao, IDemandeModifDao demandeModifDao, FileSystemStorageService storageService, LigneFichierModifService ligneFichierModifService, JournalService journalService, ReferenceService referenceService, UtilisateurService utilisateurService, Ppntoepn procStockee, @Qualifier("itemEntityManager") EntityManager entityManager) {
        super(libProfileDao);
        this.demandeModifDao = demandeModifDao;
        this.storageService = storageService;
        this.ligneFichierService = ligneFichierModifService;
        this.journalService = journalService;
        this.referenceService = referenceService;
        this.utilisateurService = utilisateurService;
        this.procStockee = procStockee;
        this.entityManager = entityManager;
    }

    @Override
    public List<DemandeDto> getAllActiveDemandesForAdminExtended() {
        List<DemandeDto> ListeDemandeDto = demandeModifDao.getAllActiveDemandesModifForAdminExtended();
        setIlnShortNameOnDemandeDtoList(ListeDemandeDto);
        return ListeDemandeDto;
    }


    /**
     * Méthode permettant de chercher les demandeModifs d'un utilisateur
     *
     * @param iln numéro de l'utilisateur propriétaire des demandeModifs
     * @return liste des demandeModifs de l'utilisateur (hors demandeModifs archivées)
     */
    @Override
    public List<DemandeDto> getActiveDemandesForUser(String iln) {
        List<DemandeDto> ListeDemandeDto = this.demandeModifDao.getActiveDemandesModifForUserExceptedPreparedStatus(iln);
        setIlnShortNameOnDemandeDtoList(ListeDemandeDto);
        return ListeDemandeDto;
    }

    @Override
    public List<DemandeDto> getAllArchivedDemandes(String iln) {
        List<DemandeDto> ListeDemandeDto = this.demandeModifDao.getAllArchivedDemandesModif(iln);
        setIlnShortNameOnDemandeDtoList(ListeDemandeDto);
        return ListeDemandeDto;
    }

    @Override
    public List<DemandeDto> getAllArchivedDemandesAllIln() {
        List<DemandeDto> ListeDemandeDto = this.demandeModifDao.getAllArchivedDemandesModifExtended();
        setIlnShortNameOnDemandeDtoList(ListeDemandeDto);
        return ListeDemandeDto;
    }

    /**
     * Méthode permettant de vérifier l'état de la demandeModif, et de déterminer
     * l'action à réaliser ensuite dans le processus global
     *
     * @param demandeModif demandeModif sur laquelle se porte la verification
     */
    private void checkEtatDemande(DemandeModif demandeModif) throws IOException, FileTypeException, DemandeCheckingException {
        int etat = demandeModif.getEtatDemande().getNumEtat();
        switch (etat) {
            case Constant.ETATDEM_PREPARATION:
                //Etat initial, préparation du fichier après appel procédure Oracle ppntoepn
                preparerFichierEnPrep(demandeModif);
                break;
            case Constant.ETATDEM_PREPAREE:
                //Fichier préparé prêt, on change juste l'état de la demandeModif
                changeState(demandeModif, Constant.ETATDEM_ACOMPLETER);
                break;
            case Constant.ETATDEM_ACOMPLETER:
                //Etat après procédure Oracle, traitement du fichier enrichi
                //appel méthode d'alimentation de la base avec les lignes du fichier
                FichierEnrichiModif fichier = (FichierEnrichiModif) FichierFactory.getFichier(demandeModif.getEtatDemande().getNumEtat(), TYPE_DEMANDE.MODIF);
                ligneFichierService.saveFile(storageService.loadAsResource(fichier.getFilename()).getFile(), demandeModif);

                String tagSubTab = fichier.getTagSubtag();
                String zone;
                int startIndex;
                if (tagSubTab.startsWith("E")) {
                    zone = tagSubTab.substring(0, 4);
                    startIndex = 4;
                } else {
                    zone = tagSubTab.substring(0, 3);
                    startIndex = 3;
                }
                demandeModif.setZone(zone);
                if (tagSubTab.length() == startIndex + 2)
                    demandeModif.setSousZone(tagSubTab.substring(startIndex, startIndex + 2));

                changeState(demandeModif, Constant.ETATDEM_SIMULATION);
                break;
            default:
        }
    }

    /**
     * méthode de préparation du fichier initial et du fichier après appel de la
     * fonction Oracle
     *
     * @param dem : demandeModif liée aux fichiers à préparer
     */
    private void preparerFichierEnPrep(DemandeModif dem) throws IOException, FileTypeException, DemandeCheckingException {
        //Suppression d'un éventuel fichier existant sur le disque
        storageService.delete(fichierPrepare.getFilename());
        //Ecriture ligne d'en-tête dans FichierApresWS
        fichierPrepare.ecrireEnTete();
        //Alimentation du fichier par appel à la procédure Oracle ppntoepn
        appelProcStockee(dem.getRcr());
        dem.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPAREE));
        save(dem);
        checkEtatDemande(dem);
    }

    /**
     * Retourne l'état précédent d'une demande dans le déroulement normal du processus
     *
     * @param etatDemande : etat dont on souhaite connaitre l'état précédent
     * @return : l'état précédent dans le processus
     */
    private int getPreviousState(int etatDemande) {
        return switch (etatDemande) {
            case Constant.ETATDEM_PREPAREE -> Constant.ETATDEM_PREPARATION;
            case Constant.ETATDEM_ACOMPLETER -> Constant.ETATDEM_PREPAREE;
            case Constant.ETATDEM_SIMULATION -> Constant.ETATDEM_ACOMPLETER;
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
     * Méthode permettant de stocker un fichier lié à une demandeModif
     *
     * @param file    : contenu du fichier à stocker sur le disque
     * @param demande : demandeModif à laquelle est rattachée le fichier
     */
    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.MODIF);
            fichier.generateFileName(demande);
            stockerFichierOnDisk(file, fichier, (DemandeModif) demande);
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }

    }

    /**
     * Méthode de stockage physique d'un fichier sur le disque
     *
     * @param file         fichier à stocker
     * @param fichier      objet fichier associé
     * @param demandeModif demandeModif associée (pour construction du filename)
     * @throws FileCheckingException type de fichier non reconnu
     * @throws IOException           demandeExemp.setExemplairesMultiplesAutorise("t");      fichier illisible
     * @throws FileTypeException     mauvais type de fichier
     */
    private void stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeModif demandeModif) throws FileCheckingException, IOException, FileTypeException, DemandeCheckingException {
        Integer numDemande = demandeModif.getNumDemande();
        try {
            storageService.changePath(Paths.get(uploadPath + "modif/" + numDemande));
            storageService.init();
            storageService.store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + "modif/" + numDemande));
            fichier.checkFileContent(demandeModif); //Controle de l'adequation des entêtes
            //Cas d'un fichier initial pouvant contenir des lignes vides à supprimer
            if (fichier.getType() == Constant.ETATDEM_PREPARATION) {
                FichierInitial fichierInitialModif = (FichierInitial) fichier;
                fichierInitialModif.supprimerRetourChariot();
            }
            checkEtatDemande(demandeModif);
        } catch (FileCheckingException e) {
            storageService.delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    /**
     * Méthode d'initialisation des fichiers nécessaires au traitement de la demandeModif
     *
     * @param demande : demandeModif concernée
     * @throws FileTypeException : Fichier non trouvé par la factory
     */
    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getNumDemande();
        /*Préparation du fichier initial rattaché à la demandeModif*/
        fichierInit = (FichierInitial) FichierFactory.getFichier(Constant.ETATDEM_PREPARATION, TYPE_DEMANDE.MODIF);
        fichierInit.generateFileName(demande);
        fichierInit.setPath(Paths.get(uploadPath + "modif/" + numDemande));
        /*Préparation du fichier résultat d'appel de la fonction Oracle*/
        fichierPrepare = (FichierPrepare) FichierFactory.getFichier(Constant.ETATDEM_PREPAREE, TYPE_DEMANDE.MODIF);
        fichierPrepare.generateFileName(demande);
        fichierPrepare.setPath(Paths.get(uploadPath + "modif/" + numDemande));
        /*Préparation du fichier enrichi par l'utilisateur*/
        FichierEnrichiModif fichierEnrichiModif = (FichierEnrichiModif) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.MODIF);
        fichierEnrichiModif.generateFileName(demande);
        fichierEnrichiModif.setPath(Paths.get(uploadPath + "modif/" + numDemande));
    }

    /**
     * Méthode de découpage du fichier initial, d'appel de la fonction Oracle et
     * d'alimentation du FichierApresWS
     *
     * @param rcr : rcr de la demandeModif
     * @throws IOException fichier illisible
     */
    private void appelProcStockee(String rcr) throws IOException {
        List<String> listppn = fichierInit.cutFile();
        for (String listeppn : listppn) {
            String resultProcStockee = procStockee.callFunction(listeppn, rcr);
            fichierPrepare.alimenterEpn(resultProcStockee, listeppn, rcr);
        }
    }


    /**
     * Lance une requête pour récupérer l'ensemble des demandeModifs
     * Lance une requête pour récupérer :
     * Les demandeModifs Terminées / En Erreur de tout le monde
     * ET toutes les demandeModifs créées par cet admin
     *
     * @return la liste de toutes les demandeModifs
     */
    @Override
    public List<DemandeDto> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeDto> listeDemandeDto = demandeModifDao.getAllActiveDemandesModifForAdmin(iln);
        setIlnShortNameOnDemandeDtoList(listeDemandeDto);
        return listeDemandeDto;
    }

    /**
     * @param id le @requestParam dans DemandeRestService, cad la paramètre récupéré via la requete HTTP (dans l'URL)
     * @return Une demandeModif identifiée par son id
     */
    @Override
    public DemandeModif findById(Integer id) {
        Optional<DemandeModif> demandeModif = demandeModifDao.findById(id);
        demandeModif.ifPresent(this::setIlnShortNameOnDemande);
        return demandeModif.orElse(null);
    }

    @Override
    public Demande save(Demande entity) {
        DemandeModif demande = (DemandeModif) entity;
        entity.setDateModification(Calendar.getInstance().getTime());
        DemandeModif demandeOut = demandeModifDao.save(demande);
        demandeOut.setShortname(entity.getShortname());
        return demandeOut;
    }

    @Override
    public void deleteById(Integer id) {
        //suppression des fichiers et du répertoire
        storageService.changePath(Paths.get(uploadPath + "modif/" + id));
        storageService.deleteAll();
        demandeModifDao.deleteById(id);
    }

    @Override
    public Demande creerDemande(String rcr, Integer userNum) {
        Calendar calendar = Calendar.getInstance();
        DemandeModif demandeModif = new DemandeModif(rcr, calendar.getTime(), calendar.getTime(), null, null, null, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), utilisateurService.findById(userNum), null);
        demandeModif.setIln(Objects.requireNonNull(libProfileDao.findById(rcr).orElse(null)).getIln());
        setIlnShortNameOnDemande(demandeModif);
        DemandeModif demToReturn = (DemandeModif) save(demandeModif);
        journalService.addEntreeJournal(demandeModif, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION));
        return demToReturn;
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
        demande.setEtatDemande(etat);
        journalService.addEntreeJournal((DemandeModif) demande, etat);
        return this.save(demande);
    }

    /**
     * Méthode permettant de passer une demandeModif dans l'état terminée
     *
     * @param demande la demandeModif à fermer
     * @return la demandeModif modifiée
     * @throws DemandeCheckingException cas où la demandeModif ne répond pas aux conditions de fermeture
     */
    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        if (ligneFichierService.getNbLigneFichierNonTraitee(demande) != 0) {
            throw new DemandeCheckingException(Constant.LINES_TO_BE_PROCESSED_REMAIN);
        }
        return changeState(demande, Constant.ETATDEM_TERMINEE);
    }



    /**
     * Permet de changer l'état d'une demandeModif
     *
     * @param demande     : demandeModif dont on souhaite changer l'état
     * @param etatDemande : état cible de la demandeModif
     * @return demandeModif modifiée
     */
    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        if ((etatDemande == Constant.ETATDEM_ERREUR) || (demande.getEtatDemande().getNumEtat() == getPreviousState(etatDemande))) {
            EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            journalService.addEntreeJournal((DemandeModif) demande, etat);
            return save(demande);
        } else {
            throw new DemandeCheckingException(Constant.DEMANDE_IS_NOT_IN_STATE + getPreviousState(etatDemande));
        }
    }

    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        List<DemandeModif> demandes = demandeModifDao.getNextDemandeToProceed();
        if (!demandes.isEmpty())
            return demandes.get(0);
        return null;
    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException, IOException {
        DemandeModif demandeModif = (DemandeModif) demande;
        int etatDemande = demande.getEtatDemande().getId();
        switch (etatDemande) {
            case Constant.ETATDEM_PREPAREE:
                resetDemande(demandeModif);
                break;
            case Constant.ETATDEM_ACOMPLETER:
                if (demandeModif.getTraitement() != null) {
                    demandeModif.setTraitement(null);
                    save(demandeModif);
                } else {
                    resetDemande(demandeModif);
                }
                break;
            default:
                throw new DemandeCheckingException(Constant.GO_BACK_TO_PREVIOUS_STEP_ON_DEMAND_FAILED);
        }
        return demande;
    }

    /**
     * @param etape   etape à laquelle on souhaite retourner
     * @param demande demande que l'on souhaite modifier
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un etat incorrect
     */
    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeModif demandeModif = (DemandeModif) demande;
        switch (etape) {
            case 1 -> demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
            case 2 -> {
                demandeModif.setTraitement(null);
                //On repasse DEM_TRAIT_ID à null : obtenu ETAPE 3
                demandeModif.setZone(null); //On repasse ZONE à null : obtenu ETAPE 5
                demandeModif.setSousZone(null); //On repasse SOUS_ZONE à null : obtenu ETAPE 5
                demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION)); //On repasse DEM_ETAT_ID à 1
                //le commentaire n'est pas effacé, il est géré dans le tableau de bord : pas dans les ETAPES
                /*Suppression des lignes de la table LIGNE_FICHIER_MODIF crées à ETAPE 5*/
                ligneFichierService.deleteByDemande(demandeModif);
            }
            case 3 -> {
                demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
                demandeModif.setTraitement(null);
                demandeModif.setZone(null);
                demandeModif.setSousZone(null);
                ligneFichierService.deleteByDemande(demandeModif);
            }
            case 4 -> {
                demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
                //On ne modifie pas le traitement obtenu a etape 3
                demandeModif.setZone(null);
                demandeModif.setSousZone(null);
                ligneFichierService.deleteByDemande(demandeModif);
            }
            default ->
                throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
        return save(demandeModif);
    }

    private void resetDemande(DemandeModif demandeModif) throws IOException {
        storageService.delete(fichierInit.getFilename());
        storageService.delete(fichierPrepare.getFilename());
        demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
        save(demandeModif);
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        DemandeModif demandeModif = (DemandeModif) demande;
        String zone = demandeModif.getZone();
        if (demandeModif.getSousZone() != null) zone += demandeModif.getSousZone();
        return "PPN;RCR;EPN;" + zone + ";RESULTAT;Demande lancée le " + dateDebut;
    }


    /**
     * méthode d'archivage d'une demande
     * supprime les lignes fichiers au moment de l'archivage
     *
     * @param demande demande à archiver
     * @return la demande dans l'état archivé
     * @throws DemandeCheckingException : problème dans l'état de la demande
     */
    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeModif demandeModif = (DemandeModif) demande;
        return changeState(demandeModif, Constant.ETATDEM_ARCHIVEE);
    }

    /**
     * Récupération de la prochaine demande terminée à archiver
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeModif> getDemandesToArchive() {
        List<DemandeModif> listeDemandes = demandeModifDao.getNextDemandeToArchive();
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
    public List<? extends Demande> getDemandesToPlaceInDeletedStatus() {
        List<DemandeModif> listeDemandes = demandeModifDao.getNextDemandeToPlaceInDeletedStatus();
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
    public List<DemandeModif> getDemandesToDelete() {
        List<DemandeModif> listeDemandes = demandeModifDao.getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    public Demande majTraitement(Integer demandeId, Integer traitementId) {
        DemandeModif demandeModif = this.findById(demandeId);
        Traitement traitement = referenceService.findTraitementById(traitementId);
        if (demandeModif != null) {
            demandeModif.setDateModification(Calendar.getInstance().getTime());
            demandeModif.setTraitement(traitement);
            demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
            return this.save(demandeModif);
        }
        return null;
    }

    @Override
    public void modifierShortNameDemande(Demande demande) {
        setIlnShortNameOnDemande(demande);
    }

    @Override
    public void cleanLignesFichierDemande(Demande demande) {
        DemandeModif demandeModif = (DemandeModif) demande;
        ligneFichierService.deleteByDemande(demandeModif);
    }

    @Override
    public void refreshEntity(Demande demande) {
        entityManager.refresh(demande);
    }
}
