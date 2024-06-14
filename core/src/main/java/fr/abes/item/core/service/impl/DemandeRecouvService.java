package fr.abes.item.core.service.impl;

import fr.abes.item.core.components.Fichier;
import fr.abes.item.core.components.FichierEnrichiRecouv;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeRecouvDao;
import fr.abes.item.core.service.*;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Strategy(type= IDemandeService.class, typeDemande = {TYPE_DEMANDE.RECOUV})
@Slf4j
public class DemandeRecouvService extends DemandeService implements IDemandeService {
    private final IDemandeRecouvDao demandeRecouvDao;
    private final FileSystemStorageService storageService;
    private final ReferenceService referenceService;
    private final TraitementService traitementService;
    private final ILigneFichierService ligneFichierService;
    private final UtilisateurService utilisateurService;
    private FichierEnrichiRecouv fichierEnrichiRecouv;


    @Value("${files.upload.path}")
    private String uploadPath;

    public DemandeRecouvService(ILibProfileDao libProfileDao, IDemandeRecouvDao demandeRecouvDao, FileSystemStorageService storageService, ReferenceService referenceService, LigneFichierRecouvService ligneFichierRecouvService, TraitementService traitementService, UtilisateurService utilisateurService) {
        super(libProfileDao);
        this.demandeRecouvDao = demandeRecouvDao;
        this.storageService = storageService;
        this.referenceService = referenceService;
        this.ligneFichierService = ligneFichierRecouvService;
        this.traitementService = traitementService;
        this.utilisateurService = utilisateurService;
    }

    public List<Demande> findAll() {
        List<Demande> liste = new ArrayList<>(demandeRecouvDao.findAll());
        setIlnShortNameOnList(new ArrayList<>(liste));
        return liste;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeRecouv> demandeRecouvs = demandeRecouvDao.getAllActiveDemandesRecouvForAdmin(iln);
        List<Demande> demandeList = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public Demande save(Demande entity) {
        DemandeRecouv demande = (DemandeRecouv) entity;
        entity.setDateModification(Calendar.getInstance().getTime());
        return demandeRecouvDao.save(demande);
    }

    @Override
    public DemandeRecouv findById(Integer id) {
        Optional<DemandeRecouv> demandeRecouv = demandeRecouvDao.findById(id);
        /*On contrôle si la demande est présente*/
        demandeRecouv.ifPresent(this::setIlnShortNameOnDemande);
        return demandeRecouv.orElse(null);
    }

    @Override
    public void deleteById(Integer id) {
        demandeRecouvDao.deleteById(id);
    }

    @Override
    public DemandeRecouv creerDemande(String rcr, Integer userNum) {
        Calendar calendar = Calendar.getInstance();
        DemandeRecouv demandeRecouv = new DemandeRecouv(rcr, calendar.getTime(), calendar.getTime(), referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), "", utilisateurService.findById(userNum));
        demandeRecouv.setIln(Objects.requireNonNull(libProfileDao.findById(rcr).orElse(null)).getIln());
        return demandeRecouv;
    }

    /**
     * Méthode permettant de chercher les demandeModifs d'un utilisateur
     * @param iln numéro de l'utilisateur propriétaire des demandeModifs
     *
     * @return liste des demandeModifs de l'utilisateur (hors demandeModifs archivées)
     */
    @Override
    public List<Demande> getActiveDemandesForUser(String iln) {
        List<DemandeRecouv> demandeRecouvs = this.demandeRecouvDao.getActiveDemandesRecouvForUserExceptedPreparedStatus(iln);
        List<Demande> listeDemande = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(listeDemande);
        return listeDemande;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeRecouv> demandeRecouv = demandeRecouvDao.getAllActiveDemandesRecouvForAdminExtended();
        List<Demande> demandeList = new ArrayList<>(demandeRecouv);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getId();
        fichierEnrichiRecouv = (FichierEnrichiRecouv) FichierFactory.getFichier(Constant.ETATDEM_PREPARATION, TYPE_DEMANDE.RECOUV);
        fichierEnrichiRecouv.generateFileName(numDemande);
        fichierEnrichiRecouv.setPath(Paths.get(uploadPath + numDemande));
        log.debug("Dépot du fichier dans : " + uploadPath + numDemande);
    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException {
        int etatDemande = demande.getEtatDemande().getId();
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        if (etatDemande == Constant.ETATDEM_ACOMPLETER) {
            demandeRecouv.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
            save(demandeRecouv);
        }
        else {
            throw new DemandeCheckingException(Constant.GO_BACK_TO_PREVIOUS_STEP_ON_DEMAND_FAILED);
        }
        return demandeRecouv;
    }

    /**
     * Méthode permettant de changer l'état d'une demande vers l'état ciblé dans le processus global de l'application
     * @param etape etape à laquelle on souhaite retourner
     * @param demande demande que l'on souhaite modifier
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un etat incorrect
     */
    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        throw new DemandeCheckingException(Constant.UNAVAILABLE_SERVICE + TYPE_DEMANDE.RECOUV);
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return changeState(demande, Constant.ETATDEM_TERMINEE);
    }

    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) {
        return new String[]{"Simulation impossible pour le recouvrement", ""};
    }

    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        List<DemandeRecouv> listeDemandes;
        if (currentHour >= minHour && currentHour <= maxHour) {
            listeDemandes = demandeRecouvDao.getNextDemandeToProceedWithoutDAT();
        } else {
            listeDemandes = demandeRecouvDao.getNextDemandeToProceed();
        }
        if (!listeDemandes.isEmpty())
            return listeDemandes.get(0);
        return null;
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return "Calcul du taux de recouvrement démarré le : " + Constant.formatDate.format(dateDebut) + "\n"
                + "requête;nb réponses;liste PPN;";
    }

    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        if (demande.getEtatDemande().getId() == getPreviousState(etatDemande) || (etatDemande == Constant.ETATDEM_ERREUR)) {
            EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            return this.save(demande);
        }
        else {
            throw new DemandeCheckingException(Constant.DEMANDE_IS_NOT_IN_STATE + getPreviousState(etatDemande));
        }
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
        demande.setEtatDemande(etat);
        return this.save(demande);
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        List<DemandeRecouv> demandeRecouvs = this.demandeRecouvDao.getAllArchivedDemandesRecouv(iln);
        List<Demande> demandeList = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        List<DemandeRecouv> demandeRecouvs = this.demandeRecouvDao.getAllArchivedDemandesRecouvExtended();
        List<Demande> demandeList = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public String stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        Integer numDemande = demande.getId();
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.RECOUV);
            fichier.generateFileName(numDemande);
            String message = stockerFichierOnDisk(file, fichier, demandeRecouv);
            this.majDemandeWithFichierEnrichi(demandeRecouv);
            return message;
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }
    }

    private void majDemandeWithFichierEnrichi(DemandeRecouv demandeRecouv) throws DemandeCheckingException, IOException {
        demandeRecouv.setIndexRecherche(fichierEnrichiRecouv.getIndexRecherche());
        ligneFichierService.saveFile(storageService.loadAsResource(fichierEnrichiRecouv.getFilename()).getFile(), demandeRecouv);
        changeState(demandeRecouv, Constant.ETATDEM_ATTENTE);
    }
    /**
     * Stockage physique du fichier de la demande sur le disque
     * @param file fichier à sauvegarder issu du client
     * @param fichier objet correspondant au fichier
     * @param demandeRecouv demande rattachée au fichier
     * @return message indiquant le bon déroulement du processus renvoyé au front
     * @throws IOException : erreur d'accès au fichier
     * @throws FileCheckingException : erreur dans le format du fichier
     */
    private String stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeRecouv demandeRecouv) throws IOException, FileCheckingException {
        Integer numDemande = demandeRecouv.getId();
        try {
            storageService.changePath(Paths.get(uploadPath + numDemande));
            storageService.init();
            storageService.store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + numDemande));
            //Ici l'objet fichierRecouv va etre renseigné avec les zones courante et valeur de ces zones
            fichier.checkFileContent(demandeRecouv);
            return Constant.MSG + file.getOriginalFilename() + " a bien été déposé sur le serveur avec le nom "
                    + fichier.getFilename();
        } catch (FileCheckingException e) {
            storageService.delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    private int getPreviousState(int etatDemande) {
        return switch (etatDemande) {
            case Constant.ETATDEM_ATTENTE -> Constant.ETATDEM_PREPARATION;
            case Constant.ETATDEM_ENCOURS -> Constant.ETATDEM_ATTENTE;
            case Constant.ETATDEM_TERMINEE -> Constant.ETATDEM_ENCOURS;
            case Constant.ETATDEM_ERREUR -> Constant.ETATDEM_ERREUR;
            case Constant.ETATDEM_ARCHIVEE -> Constant.ETATDEM_TERMINEE;
            case Constant.ETATDEM_SUPPRIMEE -> Constant.ETATDEM_ARCHIVEE;
            default -> 0;
        };
    }

    public int launchQueryToSudoc(String codeIndex, String valeurs) throws IOException, QueryToSudocException {
        String[] tabvaleurs = valeurs.split(";");
        String query = getQueryToSudoc(codeIndex, null, tabvaleurs);
        traitementService.getCbs().search(query);
        return traitementService.getCbs().getNbNotices();
    }

    /**
     * Méthode construisant la requête che en fonction des paramètres d'une demande d'exemplarisation
     * @param codeIndex code de l'index de la recherche
     * @param type : non utilisé dans cette implementation
     * @param valeur tableau des valeurs utilisées pour construire la requête
     * @return requête che prête à être lancée vers le CBS
     */
    @Override
    public String getQueryToSudoc(String codeIndex, String type, String[] valeur) throws QueryToSudocException {
        return switch (codeIndex) {
            case "ISBN" -> "che isb " + valeur[0];
            case "ISSN" -> "tno t; tdo t; che isn " + valeur[0];
            case "PPN" -> "che ppn " + valeur[0];
            case "SOU" -> "tno t; tdo b; che sou " + valeur[0];
            case "DAT" -> {
                if (valeur[1].isEmpty()) {
                    yield "tno t; tdo b; apu " + valeur[0] + "; che mti " + Utilitaires.replaceDiacritical(valeur[2]);
                }
                yield "tno t; tdo b; apu " + valeur[0] + "; che aut " + Utilitaires.replaceDiacritical(valeur[1]) + " et mti " + Utilitaires.replaceDiacritical(valeur[2]);
            }
            default -> throw new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_CODE_NOT_COMPLIANT);
        };
    }

    /** méthode d'archivage d'une demande
     * supprime les lignes fichiers au moment de l'archivage
     * @param demande demande à archiver
     * @return la demande dans l'état archivé
     * @throws DemandeCheckingException : problème dans l'état de la demande
     */
    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        ligneFichierService.deleteByDemande(demande);
        return changeState(demandeRecouv, Constant.ETATDEM_ARCHIVEE);
    }

    @Override
    public List<DemandeRecouv> getIdNextDemandeToArchive() {
        List<DemandeRecouv> listeDemandes;
        listeDemandes = demandeRecouvDao.getNextDemandeToArchive();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande archivée à placer en statut supprimé
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeRecouv> getIdNextDemandeToPlaceInDeletedStatus() {
        List<DemandeRecouv> listeDemandes;
        listeDemandes = demandeRecouvDao.getNextDemandeToPlaceInDeletedStatus();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande en statut supprimé à supprimer définitivement
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeRecouv> getIdNextDemandeToDelete() {
        List<DemandeRecouv> listeDemandes;
        listeDemandes = demandeRecouvDao.getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }
}
