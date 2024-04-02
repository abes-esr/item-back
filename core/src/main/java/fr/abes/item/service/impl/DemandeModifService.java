package fr.abes.item.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.components.Fichier;
import fr.abes.item.components.FichierEnrichiModif;
import fr.abes.item.components.FichierInitial;
import fr.abes.item.components.FichierPrepare;
import fr.abes.item.components.basexml.Ppntoepn;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.exception.FileTypeException;
import fr.abes.item.service.IDemandeModifService;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.factory.FichierFactory;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.MODIF})
public class DemandeModifService extends DemandeService implements IDemandeModifService {

    @Autowired
    Ppntoepn procStockee;

    @Value("${files.upload.path}")
    private String uploadPath;

    private FichierInitial fichierInit;
    private FichierPrepare fichierPrepare;
    private FichierEnrichiModif fichierEnrichiModif;

    @Override
    public List<Demande> findAll() {
        List<Demande> liste = new ArrayList<>();
        liste.addAll(getDao().getDemandeModif().findAll());
        return liste;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeModif> demandeModif = getDao().getDemandeModif().getAllActiveDemandesModifForAdminExtended();
        List<Demande> demandeList = new ArrayList<>(demandeModif);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }


    /**
     * Méthode permettant de chercher les demandeModifs d'un utilisateur
     * @param iln numéro de l'utilisateur propriétaire des demandeModifs
     *
     * @return liste des demandeModifs de l'utilisateur (hors demandeModifs archivées)
     */
    @Override
    public List<Demande> getActiveDemandesForUser(String iln) {
        List<DemandeModif> demandeModifs = this.getDao().getDemandeModif().getActiveDemandesModifForUserExceptedPreparedStatus(iln);
        List<Demande> listeDemande = new ArrayList<>(demandeModifs);
        setIlnShortNameOnList(listeDemande);
        return listeDemande;
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        List<DemandeModif> demandeModifs = this.getDao().getDemandeModif().getAllArchivedDemandesModif(iln);
        List<Demande> listeDemandes = new ArrayList<>(demandeModifs);
        setIlnShortNameOnList(listeDemandes);
        return listeDemandes;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln(){
        List<DemandeModif> demandeModifs = this.getDao().getDemandeModif().getAllArchivedDemandesModifExtended();
        List<Demande> listeDemandes = new ArrayList<>(demandeModifs);
        setIlnShortNameOnList(listeDemandes);
        return listeDemandes;
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

                getService().getLigneFichierModif().saveFile(getService().getStorage().loadAsResource(fichier.getFilename()).getFile(), demandeModif);

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
        getService().getStorage().delete(fichierPrepare.getFilename());
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
        switch (etatDemande) {
            case Constant.ETATDEM_PREPAREE:
                return Constant.ETATDEM_PREPARATION;
            case Constant.ETATDEM_ACOMPLETER:
                return Constant.ETATDEM_PREPAREE;
            case Constant.ETATDEM_SIMULATION:
                return Constant.ETATDEM_ACOMPLETER;
            case Constant.ETATDEM_ATTENTE:
                return Constant.ETATDEM_SIMULATION;
            case Constant.ETATDEM_ENCOURS:
                return Constant.ETATDEM_ATTENTE;
            case Constant.ETATDEM_TERMINEE:
                return Constant.ETATDEM_ENCOURS;
            case Constant.ETATDEM_ERREUR:
                return Constant.ETATDEM_ERREUR;
            case Constant.ETATDEM_ARCHIVEE:
                return Constant.ETATDEM_TERMINEE;
            case Constant.ETATDEM_SUPPRIMEE:
                return Constant.ETATDEM_ARCHIVEE;
            default:
                return 0;
        }
    }

    /**
     * Méthode permettant de stocker un fichier lié à une demandeModif
     *
     * @param file    : contenu du fichier à stocker sur le disque
     * @param demande : demandeModif à laquelle est rattachée le fichier
     * @return Message informant du bon déroulement de la méthode
     */
    @Override
    public String stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        Integer numDemande = demande.getNumDemande();
        try {
            Utilitaires.checkExtension(file.getOriginalFilename());
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.MODIF);
            fichier.generateFileName(numDemande);
            return stockerFichierOnDisk(file, fichier, (DemandeModif)demande);
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }

    }

    /**
     * Méthode de stockage physique d'un fichier sur le disque
     *
     * @param file    fichier à stocker
     * @param fichier objet fichier associé
     * @param demandeModif demandeModif associée (pour construction du filename)
     * @return message de validation du processus
     * @throws FileCheckingException type de fichier non reconnu
     * @throws IOException
        demandeExemp.setExemplairesMultiplesAutorise("t");      fichier illisible
     * @throws FileTypeException     mauvais type de fichier
     */
    private String stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeModif demandeModif) throws FileCheckingException, IOException, FileTypeException, DemandeCheckingException {
        Integer numDemande = demandeModif.getNumDemande();
        try {
            getService().getStorage().changePath(Paths.get(uploadPath + numDemande));
            getService().getStorage().init();
            getService().getStorage().store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + numDemande));
            fichier.checkFileContent(demandeModif); //Controle de l'adequation des entêtes
            //Cas d'un fichier initial pouvant contenir des lignes vides à supprimer
            if (fichier.getType() == Constant.ETATDEM_PREPARATION) {
                FichierInitial fichierInitial = (FichierInitial) fichier;
                fichierInitial.supprimerRetourChariot();
            }
            checkEtatDemande(demandeModif);
            return Constant.MSG + file.getOriginalFilename() + " a bien été déposé sur le serveur avec le nom "
                    + fichier.getFilename();

        } catch (FileCheckingException e) {
            getService().getStorage().delete(fichier.getFilename());
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
        fichierInit.generateFileName(numDemande);
        fichierInit.setPath(Paths.get(uploadPath + numDemande));
        /*Préparation du fichier résultat d'appel de la fonction Oracle*/
        fichierPrepare = (FichierPrepare) FichierFactory.getFichier(Constant.ETATDEM_PREPAREE, TYPE_DEMANDE.MODIF);
        fichierPrepare.generateFileName(numDemande);
        fichierPrepare.setPath(Paths.get(uploadPath + numDemande));
        /*Préparation du fichier enrichi par l'utilisateur*/
        FichierEnrichiModif fichierEnrichiModif = (FichierEnrichiModif) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.MODIF);
        fichierEnrichiModif.generateFileName(numDemande);
        fichierEnrichiModif.setPath(Paths.get(uploadPath + numDemande));
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
        ListIterator<String> lesppns = listppn.listIterator();
        while (lesppns.hasNext()) {
            String listeppn = lesppns.next();
            String resultProcStockee = procStockee.callFunction(listeppn, rcr);
            fichierPrepare.alimenter(resultProcStockee, listeppn, rcr);
        }
    }

    @Override
    public DemandeModif creerDemande(String rcr, Date dateCreation, Date dateModification, String zone, String sousZone, String comment, EtatDemande etatDemande, Utilisateur utilisateur, Traitement traitement) {
        DemandeModif demandeModif = new DemandeModif(rcr, dateCreation, dateModification, zone, sousZone, comment, etatDemande, utilisateur, traitement);
        demandeModif.setIln(getDao().getLibProfile().findById(rcr).orElse(null).getIln());
        return demandeModif;
    }

    /**
     * Méthode de récupération d'une ligne correspondant à une demandeModif dans la table LigneFichierModif
     *
     * @param demandeModif  demandeModif sur laquelle chercher
     * @param numLigne numéro de la ligne à récupérer
     * @return LigneFichierModif trouvée
     */
    @Override
    public LigneFichierModif getLigneFichier(DemandeModif demandeModif, Integer numLigne) {
        return this.getDao().getLigneFichierModif().getLigneFichierbyDemandeEtPos(demandeModif.getNumDemande(), numLigne);
    }


    /**
     * Méthode de récupération d'une notice par son EPN
     *
     * @param demandeModif utilisée pour récupérer le RCR qui servira pour la construction du login Manager CBS
     * @param epn          epn de la notice à chercher
     * @return La notice trouvée dans le CBS
     * @throws CBSException : erreur CBS
     */
    @Override
    public String getNoticeInitiale(DemandeModif demandeModif, String epn) throws CBSException, IOException, ZoneException {
        try {
            getService().getTraitement().authenticate('M' + demandeModif.getRcr());
            // appel getNoticeFromEPN sur EPN récupéré
            return getService().getTraitement().getNoticeFromEPN(epn);
        } finally {
            // déconnexion du CBS après avoir lancé la requête
            getService().getTraitement().disconnect();
        }
    }

    /**
     * Méthode de modification d'une notice en fonction du traitement
     *
     * @param demandeModif      permet de récupérer le traitement à lancer sur la notice
     * @param exemplaire        notice récupérée du Sudoc sur laquelle on effectue le traitement
     * @param ligneFichierModif informations à intégrer à la notice à traiter
     * @return la notice modifiée
     */
    @Override
    public Exemplaire getNoticeTraitee(DemandeModif demandeModif, String exemplaire, LigneFichierModif ligneFichierModif) throws ZoneException {
        String exempStr = Utilitaires.getExempFromNotice(exemplaire, ligneFichierModif.getEpn());
        switch (demandeModif.getTraitement().getNomMethode()) {
            case "creerNouvelleZone":
                return getService().getTraitement().creerNouvelleZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone(), ligneFichierModif.getValeurZone());
            case "supprimerZone":
                return getService().getTraitement().supprimerZone(exempStr, demandeModif.getZone());
            case "supprimerSousZone":
                return getService().getTraitement().supprimerSousZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone());
            case "ajoutSousZone":
                return getService().getTraitement().creerSousZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone(), ligneFichierModif.getValeurZone());
            case "remplacerSousZone":
                return getService().getTraitement().remplacerSousZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone(), ligneFichierModif.getValeurZone());
            default:
        }
        return null;
    }

    /**
     * Lance une requête pour récupérer l'ensemble des demandeModifs
     *
     * Lance une requête pour récupérer :
     * Les demandeModifs Terminées / En Erreur de tout le monde
     * ET toutes les demandeModifs créées par cet admin
     * @return la liste de toutes les demandeModifs
     */
    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeModif> demandeModifs = getDao().getDemandeModif().getAllActiveDemandesModifForAdmin(iln);
        List<Demande> demandeList = new ArrayList<>(demandeModifs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    /**
     * @param id le @requestParam dans DemandeRestService, cad la paramètre récupéré via la requete HTTP (dans l'URL)
     * @return Une demandeModif identifiée par son id
     */
    @Override
    public DemandeModif findById(Integer id) {
        Optional<DemandeModif> demandeModif = getDao().getDemandeModif().findById(id);
        if(demandeModif.isPresent()) {
            setIlnShortNameOnDemande(demandeModif.get());
        }
        return demandeModif.orElse(null);
    }

    @Override
    public Demande save(Demande entity) {
        DemandeModif demande = (DemandeModif) entity;
        entity.setDateModification(new Date());
        return getDao().getDemandeModif().save(demande);
    }

    @Override
    public void deleteById(Integer id) {
        //suppression des fichiers et du répertoire
        getService().getStorage().changePath(Paths.get(uploadPath + id));
        getService().getStorage().deleteAll();
        getDao().getDemandeModif().deleteById(id);
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        EtatDemande etat = getService().getReference().findEtatDemandeById(etatDemande);
        demande.setEtatDemande(etat);
        getService().getJournal().addEntreeJournal((DemandeModif) demande, etat);
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
        if (getDao().getLigneFichierModif().getNbLigneFichierNonTraitee(demande.getNumDemande()) != 0) {
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
        if ((demande.getEtatDemande().getNumEtat() == getPreviousState(etatDemande)) || (etatDemande == Constant.ETATDEM_ERREUR)) {
            EtatDemande etat = getService().getReference().findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            getService().getJournal().addEntreeJournal((DemandeModif)demande, etat);
            return save(demande);
        }
        else {
            throw new DemandeCheckingException(Constant.DEMANDE_IS_NOT_IN_STATE + getPreviousState(etatDemande));
        }
    }

    /**
     * Permet de supprimer les lignes du journal d'une demande lors de la suppression de cette demande notamment
     * @param demande demande dont on souhaite supprimer les lignes en base
     */
    public void removeEntreesJournal(Demande demande){
            getService().getJournal().removeEntreesJournal((DemandeModif)demande);
    }

    @Override
    public DemandeModif getIdNextDemandeToProceed() {
        if (!getDao().getDemandeModif().getNextDemandeToProceed().isEmpty())
            return this.getDao().getDemandeModif().getNextDemandeToProceed().get(0);
        return null;
    }

    @Override
    public List<DemandeModif> getListDemandesToClean() {
        List<DemandeModif> listeDemandes;
        listeDemandes = getDao().getDemandeModif().getListDemandesToClean();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException, IOException {
        DemandeModif demandeModif = (DemandeModif)demande;
        int etatDemande = demande.getEtatDemande().getId();
        switch (etatDemande) {
            case Constant.ETATDEM_PREPAREE:
                resetDemande(demandeModif);
                break;
            case Constant.ETATDEM_ACOMPLETER:
                if ( demandeModif.getTraitement() != null) {
                    demandeModif.setTraitement(null);
                    getService().getDemandeModif().save(demandeModif);
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
     * @param etape etape à laquelle on souhaite retourner
     * @param demande demande que l'on souhaite modifier
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un etat incorrect
     */
    @Override
    /**
     * Méthode permettant de changer l'état d'une demande vers l'état ciblé dans le processus global de l'application
     */
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeModif demandeModif = (DemandeModif) demande;
        switch (etape) {
            case 2 :
                demandeModif.setTraitement(null); //On repasse DEM_TRAIT_ID à null : obtenu ETAPE 3
                demandeModif.setZone(null); //On repasse ZONE à null : obtenu ETAPE 5
                demandeModif.setSousZone(null); //On repasse SOUS_ZONE à null : obtenu ETAPE 5
                demandeModif.setEtatDemande(new EtatDemande(1)); //On repasse DEM_ETAT_ID à 1
                //le commentaire n'est pas effacé, il est géré dans le tableau de bord : pas dans les ETAPES
                /*Suppression des lignes de la table LIGNE_FICHIER_MODIF crées à ETAPE 5*/
                this.getDao().getLigneFichierModif().deleteLigneFichierModifByDemandeExempId(demandeModif.getId());
                //Mise à jour de l'entité
                getService().getDemandeModif().save(demandeModif);
                //Suppression du fichier sur disque non nécessaire, sera écrasé au prochain upload
                return demandeModif;
            case 3 :
                demandeModif.setEtatDemande(new EtatDemande(3));
                demandeModif.setTraitement(null);
                demandeModif.setZone(null);
                demandeModif.setSousZone(null);
                this.getDao().getLigneFichierModif().deleteLigneFichierModifByDemandeExempId(demandeModif.getId());
                getService().getDemandeModif().save(demandeModif);
                return demandeModif;
            case 4 :
                demandeModif.setEtatDemande(new EtatDemande(3));
                //On ne modifie pas le traitement obtenu a etape 3
                demandeModif.setZone(null);
                demandeModif.setSousZone(null);
                this.getDao().getLigneFichierModif().deleteLigneFichierModifByDemandeExempId(demandeModif.getId());
                getService().getDemandeModif().save(demandeModif);
                return demandeModif;
            default :
                throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
    }

    private void resetDemande(DemandeModif demandeModif) throws IOException{
        getService().getStorage().delete(fichierInit.getFilename());
        getService().getStorage().delete(fichierPrepare.getFilename());
        demandeModif.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
        getService().getDemandeModif().save(demandeModif);
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, Date dateDebut) {
        DemandeModif demandeModif = (DemandeModif) demande;
        String zone = demandeModif.getZone();
        if (demandeModif.getSousZone() != null) zone += demandeModif.getSousZone();
        return "PPN;RCR;EPN;" + zone + ";RESULTAT;Demande lancée le" + dateDebut;
    }

    /**
     * Méthode de récupération des informations à écrire dans le pied de page du fichier résultat : modification non concernée
     * @param demande
     * @return
     */
    @Override
    public String getInfoFooterFichierResultat(Demande demande) {
        return "";
    }

    /** méthode d'archivage d'une demande
     * supprime les lignes fichiers au moment de l'archivage
     * @param demande demande à archiver
     * @return la demande dans l'état archivé
     * @throws DemandeCheckingException
     */
    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeModif demandeModif = (DemandeModif) demande;
        getService().getLigneFichierModif().deleteByDemande(demande);
        return getService().getDemandeModif().changeState(demandeModif, Constant.ETATDEM_ARCHIVEE);
    }

    /**
     * Récupération de la prochaine demande terminée à archiver
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeModif> getIdNextDemandeToArchive() {
        List<DemandeModif> listeDemandes;
        listeDemandes = getDao().getDemandeModif().getNextDemandeToArchive();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande archivée à placer en statut supprimé
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeModif> getIdNextDemandeToPlaceInDeletedStatus() {
        List<DemandeModif> listeDemandes;
        listeDemandes = getDao().getDemandeModif().getNextDemandeToPlaceInDeletedStatus();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande en statut supprimé à supprimer définitivement
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeModif> getIdNextDemandeToDelete() {
        List<DemandeModif> listeDemandes;
        listeDemandes = getDao().getDemandeModif().getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }
}
