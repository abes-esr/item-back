package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.components.*;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeSuppDao;
import fr.abes.item.core.service.*;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.SUPP})
public class DemandeSuppService extends DemandeService implements IDemandeService {
    private final IDemandeSuppDao demandeSuppDao;
    private final ILigneFichierService ligneFichierService;
    private final ReferenceService referenceService;
    private final UtilisateurService utilisateurService;
    private final FileSystemStorageService storageService;
    private final TraitementService traitementService;

    private FichierInitialSupp fichierInit;
    private FichierPrepareSupp fichierPrepare;
    private final Ppntoepn procStockeePpnToEpn;
    private final Epntoppn procStockeeEpnToPpn;

    @Value("${files.upload.path}")
    private String uploadPath;

    public DemandeSuppService(ILibProfileDao libProfileDao, IDemandeSuppDao demandeSuppDao, FileSystemStorageService storageService, ReferenceService referenceService, UtilisateurService utilisateurService, Ppntoepn procStockeePpnToEpn, Epntoppn procStockeeEpnToPpn, LigneFichierSuppService ligneFichierSuppService, TraitementService traitementService) {
        super(libProfileDao);
        this.demandeSuppDao = demandeSuppDao;
        this.storageService = storageService;
        this.referenceService = referenceService;
        this.utilisateurService = utilisateurService;
        this.procStockeePpnToEpn = procStockeePpnToEpn;
        this.procStockeeEpnToPpn = procStockeeEpnToPpn;
        this.ligneFichierService = ligneFichierSuppService;
        this.traitementService = traitementService;
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
        return save(demandeSupp);
    }

    @Override
    public void modifierShortNameDemande(Demande demande) {
        setIlnShortNameOnDemande(demande);
    }

    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        ligneFichierService.deleteByDemande(demandeSupp);
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
            if(demande.getTypeSuppression().equals(TYPE_SUPPRESSION.EPN))
                fichierPrepare.controleIntegriteDesCorrespondances();
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
    public List<Demande> getActiveDemandesForUser(String iln) {
        List<DemandeSupp> demandesSupp = this.demandeSuppDao.getActiveDemandesSuppForUserExceptedPreparedStatus(iln);
        List<Demande> listeDemande = new ArrayList<>(demandesSupp);
        setIlnShortNameOnList(listeDemande);
        return listeDemande;
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
        if ((etatDemande == Constant.ETATDEM_ERREUR) || (demande.getEtatDemande().getNumEtat() == getPreviousState(etatDemande))) {
            EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
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
            default -> 0;
        };
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        //todo
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        List<DemandeSupp> demandesSupp = this.demandeSuppDao.getAllArchivedDemandesSupp(iln);
        List<Demande> listeDemandes = new ArrayList<>(demandesSupp);
        setIlnShortNameOnList(listeDemandes);
        return listeDemandes;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        List<DemandeSupp> demandesSupp = this.demandeSuppDao.getAllArchivedDemandesSuppExtended();
        List<Demande> listeDemandes = new ArrayList<>(demandesSupp);
        setIlnShortNameOnList(listeDemandes);
        return listeDemandes;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeSupp> demandeSupps = demandeSuppDao.getAllActiveDemandesSuppForAdminExtended();
        List<Demande> demandesList = new ArrayList<>(demandeSupps);
        setIlnShortNameOnList(demandesList);
        return demandesList;
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
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeSupp> demandesSupp = demandeSuppDao.getAllActiveDemandesSuppForAdmin(iln);
        List<Demande> demandeList = new ArrayList<>(demandesSupp);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        switch (etape) {
            //étape sélection du type de fichier de suppression
            case 1 -> {
                demandeSupp.setTypeSuppression(null);
                return save(demandeSupp);
            }
            //étape upload du fichier
            case 2 -> {
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
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws CBSException, ZoneException, IOException {
        LigneFichierSupp ligneFichierSupp = (LigneFichierSupp) ligneFichier;
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        try {
            traitementService.authenticate("M" + demandeSupp.getRcr());
            List<Exemplaire> exemplairesExistants = getExemplairesExistants(ligneFichierSupp);
            //On ne conserve que les EPN de son RCR
            exemplairesExistants = exemplairesExistants.stream().filter(exemplaire -> exemplaire.findZone("930", 0).findSubLabel("$b").equals(demandeSupp.getRcr())).toList();
            if (exemplairesExistants.isEmpty()) {
                return new String[] {
                        ligneFichierSupp.getPpn(),
                        "Pas d'exemplaire pour ce RCR",
                        "Pas d'exemplaire pour ce RCR"
                };
            }
            List<Exemplaire> exemplairesRestants = suppExemlaire(exemplairesExistants, ligneFichierSupp.getEpn());

            return new String[]{
                    ligneFichierSupp.getPpn(),
                    exemplairesExistants.stream().map(exemplaire -> exemplaire.toString().replace("\r", "\r\n")).collect(Collectors.joining("\r\n\r\n")),
                    exemplairesRestants.stream().map(exemplaire -> exemplaire.toString().replace("\r", "\r\n")).collect(Collectors.joining("\r\n\r\n"))
            };
        }catch (QueryToSudocException ex) {
            throw new CBSException(Level.ERROR, ex.getMessage());
        } finally {
            traitementService.disconnect();
        }
    }

    public List<Exemplaire> getExemplairesExistants(LigneFichierSupp ligneFichierSupp) throws IOException, QueryToSudocException, CBSException, ZoneException {
        return getExemplairesExistants(ligneFichierSupp.getPpn());
    }

    public List<Exemplaire> getExemplairesExistants(String ppn) throws IOException, QueryToSudocException, CBSException, ZoneException {
        return getExemplairesExistantsInternal(ppn);
    }

    private List<Exemplaire> getExemplairesExistantsInternal(String ppn) throws IOException, QueryToSudocException, CBSException, ZoneException {
        String query = "che ppn " + ppn;
        traitementService.getCbs().search(query);
        int nbReponses = traitementService.getCbs().getNbNotices();
        return switch (nbReponses) {
            case 0 -> throw new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND);
            case 1 -> {
                String notice = traitementService.getCbs().getClientCBS().mod("1", String.valueOf(traitementService.getCbs().getLotEncours()));
                String exemplaires = Utilitaires.getExemplairesExistants(notice);
                List<Exemplaire> exempList = new ArrayList<>();
                if (!exemplaires.isEmpty()) {
                    for (String s : exemplaires.split("\r\r\r")) {
                        if (!s.isEmpty())
                            exempList.add(new Exemplaire(s));
                    }
                }
                yield exempList;
            }
            default ->
                    throw new QueryToSudocException(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + traitementService.getCbs().getListePpn());
        };
    }

    private List<Exemplaire> suppExemlaire(List<Exemplaire> exemplairesExistants, String epn) {
        return exemplairesExistants.stream().filter(exemplaire -> !exemplaire.findZone("A99", 0).getValeur().equals(epn)).collect(Collectors.toList());
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
    public String getQueryToSudoc(String code, Integer type, String[] valeurs) throws QueryToSudocException {
        return null;
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
}
