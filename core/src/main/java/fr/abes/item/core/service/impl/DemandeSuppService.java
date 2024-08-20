package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.components.*;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeSuppDao;
import fr.abes.item.core.service.FileSystemStorageService;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.SUPP})
public class DemandeSuppService extends DemandeService implements IDemandeService {
    private static final Integer nbIdMaxPerRequest = 300;

    private final IDemandeSuppDao demandeSuppDao;
    private final ReferenceService referenceService;
    private final UtilisateurService utilisateurService;
    private final FileSystemStorageService storageService;

    private FichierInitialSupp fichierInit;
    private FichierPrepareSupp fichierPrepare;
    private final Ppntoepn procStockeePpnToEpn;
    private final Epntoppn procStockeeEpnToPpn;

    @Value("${files.upload.path}")
    private String uploadPath;

    public DemandeSuppService(ILibProfileDao libProfileDao, IDemandeSuppDao demandeSuppDao, FileSystemStorageService storageService, ReferenceService referenceService, UtilisateurService utilisateurService, Ppntoepn procStockeePpnToEpn, Epntoppn procStockeeEpnToPpn) {
        super(libProfileDao);
        this.demandeSuppDao = demandeSuppDao;
        this.storageService = storageService;
        this.referenceService = referenceService;
        this.utilisateurService = utilisateurService;
        this.procStockeePpnToEpn = procStockeePpnToEpn;
        this.procStockeeEpnToPpn = procStockeeEpnToPpn;
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
        return demToReturn;
    }

    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getId();
        /*Préparation du fichier initial rattaché à la demande de suppression */
        fichierInit = (FichierInitialSupp) FichierFactory.getFichier(Constant.ETATDEM_PREPARATION, TYPE_DEMANDE.SUPP);
        fichierInit.generateFileName(numDemande);
        fichierInit.setPath(Paths.get(uploadPath + "supp/" + numDemande));

        /*Préparation du fichier enrichi suite l'appel à la fonction oracle */
        fichierPrepare = (FichierPrepareSupp) FichierFactory.getFichier(Constant.ETATDEM_PREPAREE, TYPE_DEMANDE.SUPP);
        fichierPrepare.generateFileName(numDemande);
        fichierPrepare.setPath(Paths.get(uploadPath + "supp/" + numDemande));

        /*Préparation du fichier enrichi par l'utilisateur */
        FichierEnrichiSupp fichierEnrichiSupp = (FichierEnrichiSupp) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.SUPP);
        fichierEnrichiSupp.generateFileName(numDemande);
        fichierEnrichiSupp.setPath(Paths.get(uploadPath + "supp/" + numDemande));

    }

    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        Integer numDemande = demande.getNumDemande();
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.SUPP);
            fichier.generateFileName(numDemande);
            stockerFichierOnDisk(file, fichier, (DemandeSupp) demande);
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }
    }

    private void stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeSupp demande) throws IOException, FileCheckingException, DemandeCheckingException {
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

    private void checkEtatDemande(DemandeSupp demande) throws DemandeCheckingException, IOException {
        int etat = demande.getEtatDemande().getNumEtat();
        switch (etat) {
            case Constant.ETATDEM_PREPARATION -> preparerFichierEnPrep(demande);
            case Constant.ETATDEM_PREPAREE -> changeState(demande, Constant.ETATDEM_ACOMPLETER);
            case Constant.ETATDEM_ACOMPLETER -> changeState(demande, Constant.ETATDEM_SIMULATION);
        }
    }

    private void preparerFichierEnPrep(DemandeSupp demande) throws IOException, DemandeCheckingException {
        //Suppression d'un éventuel fichier existant sur le disque
        storageService.delete(fichierPrepare.getFilename());
        //Ecriture ligne d'en-tête dans FichierApresWS
        fichierPrepare.ecrireEnTete();
        //Alimentation du fichier par appel à la procédure Oracle ppntoepn
        appelProcStockee(demande.getRcr(), demande.getTypeSuppression());
        demande.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPAREE));
        save(demande);
        checkEtatDemande(demande);    //todo ajouter le check etat demande
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
        return null;
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public List<Demande> getActiveDemandesForUser(String iln) {
        return null;
    }

    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        return null;
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return null;
    }

    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        return null;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeSupp> demandeSupps = demandeSuppDao.getAllActiveDemandesModifForAdminExtended();
        List<Demande> demandesList = new ArrayList<>(demandeSupps);
        setIlnShortNameOnList(demandesList);
        return demandesList;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        return null;
    }

    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        switch (etape) {
            //étape choix du RCR et choix du type de fichier de suppression
            case 2 -> {
                demandeSupp.setTypeSuppression(null); //On repasse TYPE_SUPPRESSION à null : obtenu ETAPE 3
                demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION)); //On repasse DEM_ETAT_ID à 1
                //le commentaire n'est pas effacé, il est géré dans le tableau de bord : pas dans les ETAPES
                //Suppression des lignes de la table LIGNE_FICHIER_SUPP crées à ETAPE 5
                return save(demandeSupp);
                //Suppression du fichier sur disque non nécessaire, sera écrasé au prochain upload
            }
            //etape upload du fichier initial
            case 3 -> {
                demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
                return save(demandeSupp);
                //Suppression du fichier sur disque non nécessaire, sera écrasé au prochain upload
            }
            default -> throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
    }

    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws
            CBSException, ZoneException, IOException {
        return new String[0];
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToArchive() {
        return null;
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToPlaceInDeletedStatus() {
        return null;
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToDelete() {
        return null;
    }

    @Override
    public String getQueryToSudoc(String code, String type, String[] valeurs) throws QueryToSudocException {
        return null;
    }

    public Demande majTypeSupp(Integer demandeId, TYPE_SUPPRESSION typeSuppression) {
        DemandeSupp demandeSupp = this.findById(demandeId);
        if (demandeSupp != null) {
            demandeSupp.setDateModification(Calendar.getInstance().getTime());
            demandeSupp.setTypeSuppression(typeSuppression);
            demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPAREE));
            return this.save(demandeSupp);
        }
        return null;
    }
}
