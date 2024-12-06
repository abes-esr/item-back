package fr.abes.item.core.service.impl;

import fr.abes.item.core.components.*;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeSuppDao;
import fr.abes.item.core.service.*;
import fr.abes.item.core.utilitaire.Utilitaires;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.SUPP})
public class DemandeSuppService extends DemandeService implements IDemandeService {
    private final IDemandeSuppDao demandeSuppDao;
    private final ILigneFichierService ligneFichierService;
    private final ReferenceService referenceService;
    private final UtilisateurService utilisateurService;
    private final FileSystemStorageService storageService;
    private final JournalService journalService;
    private FichierInitialSupp fichierInit;
    private FichierPrepareSupp fichierPrepare;
    private final Ppntoepn procStockeePpnToEpn;
    private final Epntoppn procStockeeEpnToPpn;

    @Value("${files.upload.path}")
    private String uploadPath;

    public DemandeSuppService(ILibProfileDao libProfileDao, IDemandeSuppDao demandeSuppDao, FileSystemStorageService storageService, ReferenceService referenceService, UtilisateurService utilisateurService, Ppntoepn procStockeePpnToEpn, Epntoppn procStockeeEpnToPpn, LigneFichierSuppService ligneFichierSuppService, @Qualifier("itemEntityManager") EntityManager entityManager, JournalService journalService) {
        super(libProfileDao, entityManager);
        this.demandeSuppDao = demandeSuppDao;
        this.storageService = storageService;
        this.referenceService = referenceService;
        this.utilisateurService = utilisateurService;
        this.procStockeePpnToEpn = procStockeePpnToEpn;
        this.procStockeeEpnToPpn = procStockeeEpnToPpn;
        this.ligneFichierService = ligneFichierSuppService;
        this.journalService = journalService;
    }

    @Override
    public Demande save(Demande entity) {
        DemandeSupp demande = (DemandeSupp) entity;
        entity.setDateModification(Calendar.getInstance().getTime());
        DemandeSupp demandeOut = demandeSuppDao.save(demande);
        demandeOut.setShortname(entity.getShortname());
        return demandeOut;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DemandeSupp findById(Integer id) {
        Optional<DemandeSupp> demandeSupp = demandeSuppDao.findById(id);
        demandeSupp.ifPresent(this::setIlnShortNameOnDemande);
        return demandeSupp.orElse(null);
    }

    @Override
    public Demande creerDemande(String rcr, Integer userNum) {
        Calendar calendar = Calendar.getInstance();
        DemandeSupp demandeSupp = new DemandeSupp(rcr, calendar.getTime(), calendar.getTime(), null, null, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), utilisateurService.findById(userNum));
        demandeSupp.setIln(Objects.requireNonNull(libProfileDao.findById(rcr).orElse(null)).getIln());
        setIlnShortNameOnDemande(demandeSupp);
        DemandeSupp demToReturn = (DemandeSupp) save(demandeSupp);
        journalService.addEntreeJournal(demToReturn, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION));
        return demToReturn;
    }

    @Override
    public void modifierShortNameDemande(Demande demande) {
        setIlnShortNameOnDemande(demande);
    }

    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        return changeState(demandeSupp, Constant.ETATDEM_ARCHIVEE);
    }

    @Override
    public void deleteById(Integer id) {
        //suppression des fichiers et du répertoire
        storageService.changePath(Paths.get(uploadPath + "supp/" +  id));
        storageService.deleteAll();
        demandeSuppDao.deleteById(id);
    }

    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getId();
        /*Préparation du fichier initial rattaché à la demande de suppression */
        fichierInit = (FichierInitialSupp) FichierFactory.getFichier(Constant.ETATDEM_PREPARATION, TYPE_DEMANDE.SUPP);
        fichierInit.generateFileName(demande);
        fichierInit.setPath(Paths.get(uploadPath + "supp/" + numDemande));

        /*Préparation du fichier enrichi suite l'appel à la fonction oracle */
        fichierPrepare = (FichierPrepareSupp) FichierFactory.getFichier(Constant.ETATDEM_PREPAREE, TYPE_DEMANDE.SUPP);
        fichierPrepare.generateFileName(demande);
        fichierPrepare.setPath(Paths.get(uploadPath + "supp/" + numDemande));

        /*Préparation du fichier enrichi par l'utilisateur */
        FichierEnrichiSupp fichierEnrichiSupp = (FichierEnrichiSupp) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.SUPP);
        fichierEnrichiSupp.generateFileName(demande);
        fichierEnrichiSupp.setPath(Paths.get(uploadPath + "supp/" + numDemande));

    }

    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.SUPP);
            fichier.generateFileName(demande);
            stockerFichierOnDisk(file, fichier, (DemandeSupp) demande);
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }
    }

    private void stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeSupp demande) throws IOException, FileCheckingException, DemandeCheckingException, FileTypeException {
        Integer numDemande = demande.getNumDemande();
        try {
            storageService.changePath(Paths.get(uploadPath + "supp/" + numDemande));
            storageService.init();
            storageService.store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + "supp/" + numDemande));
            fichier.checkFileContent(demande);
            //suppression des lignes vides d'un fichier initial de ppn / epn
            if (fichier.getType() == Constant.ETATDEM_PREPARATION) {
                FichierInitialSupp fichierInitialSupp = (FichierInitialSupp) fichier;
                fichierInitialSupp.supprimerRetourChariot();
            }
            checkEtatDemande(demande);
        } catch (FileCheckingException e) {
            storageService.delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    private void checkEtatDemande(DemandeSupp demande) throws DemandeCheckingException, IOException, FileTypeException, FileCheckingException {
        int etat = demande.getEtatDemande().getNumEtat();
        switch (etat) {
            case Constant.ETATDEM_PREPARATION -> preparerFichierEnPrep(demande);
            case Constant.ETATDEM_PREPAREE -> changeState(demande, Constant.ETATDEM_ACOMPLETER);
            case Constant.ETATDEM_ACOMPLETER -> {
                //Etat après procédure Oracle, traitement du fichier enrichi
                //appel méthode d'alimentation de la base avec les lignes du fichier
                FichierEnrichiSupp fichier = (FichierEnrichiSupp) FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.SUPP);

                ligneFichierService.saveFile(storageService.loadAsResource(fichier.getFilename()).getFile(), demande);

                changeState(demande, Constant.ETATDEM_SIMULATION);
            }
        }
    }

    private void preparerFichierEnPrep(DemandeSupp demande) throws IOException, DemandeCheckingException, FileTypeException, FileCheckingException {
        if (demande.getTypeSuppression() != null) {
            //Suppression d'un éventuel fichier existant sur le disque
            storageService.delete(fichierPrepare.getFilename());
            //Ecriture ligne d'en-tête dans FichierApresWS
            fichierPrepare.ecrireEnTete();
            //Alimentation du fichier par appel à la procédure Oracle ppntoepn
            appelProcStockee(demande.getRcr(), demande.getTypeSuppression());
            fichierPrepare.trierLignesDeCorrespondances();
            demande.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPAREE));
            save(demande);
            checkEtatDemande(demande);
        }
    }

    /**
     * Méthode de découpage du fichier initial, d'appel de la fonction Oracle et
     * d'alimentation du FichierApresWS
     *
     * @param rcr : rcr de la demandeSupp
     * @throws IOException fichier illisible
     */
    private void appelProcStockee(String rcr, TYPE_SUPPRESSION type) throws IOException {
        if (type.equals(TYPE_SUPPRESSION.PPN)) {
            List<String> listppn = fichierInit.cutFile();
            for (String listePpn : listppn) {
                String resultProcStockee = procStockeePpnToEpn.callFunction(listePpn, rcr);
                fichierPrepare.alimenterEpn(resultProcStockee, listePpn, rcr);
            }
        } else {
            List<String> listEpn = fichierInit.cutFile();
            for (String listeepn : listEpn) {
                String resultProcStockee = procStockeeEpnToPpn.callFunction(listeepn, rcr);
                fichierPrepare.alimenterPpn(resultProcStockee, listeepn, rcr);
            }
        }
    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException, IOException {
        //todo
        return null;
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        if (ligneFichierService.getNbLigneFichierNonTraitee(demande) != 0) {
            throw new DemandeCheckingException(Constant.LINES_TO_BE_PROCESSED_REMAIN);
        }
        return changeState(demande, Constant.ETATDEM_TERMINEE);
    }


    /**
     * Méthode permettant de chercher les demandesSupp d'un utilisateur
     *
     * @param iln numéro de l'utilisateur propriétaire des demandesSupp
     * @return liste des demandesSupp de l'utilisateur (hors demandesSupp archivées)
     */
    @Override
    public List<DemandeDto> getActiveDemandesForUser(String iln) {
        List<DemandeDto> listeDemandesDto = this.demandeSuppDao.getActiveDemandesSuppForUserExceptedPreparedStatus(iln);
        setIlnShortNameOnDemandeDtoList(listeDemandesDto);
        return listeDemandesDto;
    }

    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        List<DemandeSupp> demandesSupp = this.demandeSuppDao.findDemandeSuppsByEtatDemande_IdOrderByDateModificationAsc(Constant.ETATDEM_ATTENTE);
        return demandesSupp.isEmpty() ? null : demandesSupp.get(0);
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return "PPN;RCR;EPN;RESULTAT;Demande lancée le " + dateDebut;
    }

    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        if ((etatDemande == Constant.ETATDEM_ERREUR) || (etatDemande == Constant.ETATDEM_SUPPRIMEE)
                || (etatDemande == Constant.ETATDEM_INTERROMPUE && (demande.getEtatDemande().getNumEtat() == Constant.ETATDEM_ENCOURS || demande.getEtatDemande().getNumEtat() == Constant.ETATDEM_ATTENTE))
                || (demande.getEtatDemande().getNumEtat() == getPreviousState(etatDemande))
                || (etatDemande == Constant.ETATDEM_ARCHIVEE && demande.getEtatDemande().getNumEtat() == Constant.ETATDEM_INTERROMPUE)) {
            EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            journalService.addEntreeJournal((DemandeSupp) demande, etat);
            return save(demande);
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

    @Override
    public List<DemandeDto> getAllArchivedDemandes(String iln) {
        List<DemandeDto> listeDemandesDto = this.demandeSuppDao.getAllArchivedDemandesSupp(iln);
        setIlnShortNameOnDemandeDtoList(listeDemandesDto);
        return listeDemandesDto;
    }

    @Override
    public List<DemandeDto> getAllArchivedDemandesAllIln() {
        List<DemandeDto> listeDemandesDto = this.demandeSuppDao.getAllArchivedDemandesSuppExtended();
        setIlnShortNameOnDemandeDtoList(listeDemandesDto);
        return listeDemandesDto;
    }

    @Override
    public List<DemandeDto> getAllActiveDemandesForAdminExtended() {
        List<DemandeDto> listeDemandesDto = demandeSuppDao.getAllActiveDemandesSuppForAdminExtended();
        setIlnShortNameOnDemandeDtoList(listeDemandesDto);
        return listeDemandesDto;
    }

    /**
     * Lance une requête pour récupérer l'ensemble des demandesSupp
     * Lance une requête pour récupérer :
     * Les demandesSupp Terminées / En Erreur de tout le monde
     * ET toutes les demandesSupp créées par cet admin
     *
     * @return la liste de toutes les demandesSupp
     */
    @Override
    public List<DemandeDto> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeDto> listeDemandesDto = demandeSuppDao.getAllActiveDemandesSuppForAdmin(iln);
        setIlnShortNameOnDemandeDtoList(listeDemandesDto);
        return listeDemandesDto;
    }

    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        switch (etape) {
            //étape sélection du type de fichier de suppression
            case 1 -> {
                demandeSupp.setTypeSuppression(null);
                demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));

            }
            //étape upload du fichier
            case 2 -> demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION)); //On repasse DEM_ETAT_ID à 1
            //etape upload du fichier initial
            case 3 -> demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
            default -> throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
        return save(demandeSupp);
    }



    @Override
    public List<? extends Demande> getDemandesToArchive() {
        List<DemandeSupp> listeDemandes = demandeSuppDao.getNextDemandeToArchive();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public List<? extends Demande> getDemandesToPlaceInDeletedStatus() {
        List<DemandeSupp> listeDemandes = demandeSuppDao.getNextDemandeToPlaceInDeletedStatus();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public List<? extends Demande> getDemandesToDelete() {
        List<DemandeSupp> listeDemandes = demandeSuppDao.getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public Demande restaurerDemande(Demande demande) throws DemandeCheckingException {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        if (demandeSupp.getEtatDemande().getNumEtat() != Constant.ETATDEM_ARCHIVEE)
            throw new DemandeCheckingException("La demande doit être en état archivée !");
        EtatDemande etat = journalService.getDernierEtatConnuAvantArchivage(demandeSupp);
        if (etat != null) {
            demandeSupp.setEtatDemande(etat);
            journalService.addEntreeJournal(demandeSupp,etat);
            return save(demandeSupp);
        }
        return demande;
    }


    public Demande majTypeSupp(Integer demandeId, TYPE_SUPPRESSION typeSuppression) {
        DemandeSupp demandeSupp = this.findById(demandeId);
        if (demandeSupp != null) {
            demandeSupp.setDateModification(Calendar.getInstance().getTime());
            demandeSupp.setTypeSuppression(typeSuppression);
            return this.save(demandeSupp);
        }
        return null;
    }

    @Override
    public void refreshEntity(Demande demande) {
        entityManager.refresh(demande);
    }


    @Override
    public void cleanLignesFichierDemande(Demande demande) {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        ligneFichierService.deleteByDemande(demandeSupp);
    }
}
