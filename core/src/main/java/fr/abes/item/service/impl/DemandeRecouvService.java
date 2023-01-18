package fr.abes.item.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.item.components.Fichier;
import fr.abes.item.components.FichierEnrichiRecouv;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.exception.FileTypeException;
import fr.abes.item.exception.QueryToSudocException;
import fr.abes.item.service.IDemandeRecouvService;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.factory.FichierFactory;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.utilitaire.Utilitaires;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
@Strategy(type= IDemandeService.class, typeDemande = {TYPE_DEMANDE.RECOUV})
public class DemandeRecouvService extends DemandeService implements IDemandeRecouvService {
    private FichierEnrichiRecouv fichierEnrichiRecouv;

    @Value("${files.upload.path}")
    private String uploadPath;

    @Override
    public List<Demande> findAll() {
        List<Demande> liste = new ArrayList<>();
        liste.addAll(getDao().getDemandeRecouv().findAll());
        setIlnShortNameOnList(new ArrayList<>(liste));
        return liste;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeRecouv> demandeRecouvs = getDao().getDemandeRecouv().getAllActiveDemandesRecouvForAdmin(iln);
        List<Demande> demandeList = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public Demande save(Demande entity) {
        DemandeRecouv demande = (DemandeRecouv) entity;
        entity.setDateModification(new Date());
        return getDao().getDemandeRecouv().save(demande);
    }

    @Override
    public Demande findById(Integer id) {
        Optional<DemandeRecouv> demandeRecouv = getDao().getDemandeRecouv().findById(id);
        if (demandeRecouv.isPresent()) { /*On contrôle si la demande est présente*/
            setIlnShortNameOnDemande(demandeRecouv.get());
        }
        return demandeRecouv.orElse(null);
    }

    @Override
    public void deleteById(Integer id) {
        getDao().getDemandeRecouv().deleteById(id);
    }

    @Override
    public DemandeRecouv creerDemande(String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        DemandeRecouv demandeRecouv = new DemandeRecouv(rcr, dateCreation, dateModification, etatDemande, commentaire, utilisateur);
        demandeRecouv.setIln(getDao().getLibProfile().findById(rcr).orElse(null).getIln());
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
        List<DemandeRecouv> demandeRecouvs = this.getDao().getDemandeRecouv().getActiveDemandesRecouvForUserExceptedPreparedStatus(iln);
        List<Demande> listeDemande = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(listeDemande);
        return listeDemande;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeRecouv> demandeRecouv = getDao().getDemandeRecouv().getAllActiveDemandesRecouvForAdminExtended();
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
    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException, IOException {
        int etatDemande = demande.getEtatDemande().getId();
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        if (etatDemande == Constant.ETATDEM_ACOMPLETER) {
            demandeRecouv.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
            getService().getDemandeRecouv().save(demandeRecouv);
        }
        else {
            throw new DemandeCheckingException(Constant.GO_BACK_TO_PREVIOUS_STEP_ON_DEMAND_FAILED);
        }
        return demandeRecouv;
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
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        switch (etape) {
            case 2 :
                return demandeRecouv;
            default :
                throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return changeState(demande, Constant.ETATDEM_TERMINEE);
    }

    @Override
    public Demande getIdNextDemandeToProceed() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        List<DemandeRecouv> listeDemandes;
        if (currentHour >= minHour && currentHour <= maxHour) {
            listeDemandes = getDao().getDemandeRecouv().getNextDemandeToProceedWithoutDAT();
        } else {
            listeDemandes = getDao().getDemandeRecouv().getNextDemandeToProceed();
        }
        if (!listeDemandes.isEmpty())
            return listeDemandes.get(0);
        return null;
    }

    @Override
    public Demande getIdNextDemandeToClean() {
        List<DemandeRecouv> listeDemandes;
        listeDemandes = getDao().getDemandeRecouv().getNextDemandeToClean();
        if (!listeDemandes.isEmpty())
            return listeDemandes.get(0);
        return null;
    }

    @Override
    public List<DemandeRecouv> getListDemandesToClean() {
        List<DemandeRecouv> listeDemandes;
        listeDemandes = getDao().getDemandeRecouv().getListDemandesToClean();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, Date dateDebut) {
        return "Calcul du taux de recouvrement démarré le : " + Constant.formatDate.format(dateDebut) + "\n"
                + "requête;nb réponses;liste PPN;";
    }

    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        if (demande.getEtatDemande().getId() == getPreviousState(etatDemande) || (etatDemande == Constant.ETATDEM_ERREUR)) {
            EtatDemande etat = getService().getReference().findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            return this.save(demande);
        }
        else {
            throw new DemandeCheckingException(Constant.DEMANDE_IS_NOT_IN_STATE + getPreviousState(etatDemande));
        }
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        EtatDemande etat = getService().getReference().findEtatDemandeById(etatDemande);
        demande.setEtatDemande(etat);
        return this.save(demande);
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        List<DemandeRecouv> demandeRecouvs = this.getDao().getDemandeRecouv().getAllArchivedDemandesRecouv(iln);
        List<Demande> demandeList = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        List<DemandeRecouv> demandeRecouvs = this.getDao().getDemandeRecouv().getAllArchivedDemandesRecouvExtended();
        List<Demande> demandeList = new ArrayList<>(demandeRecouvs);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public String stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        Integer numDemande = demande.getId();
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        try {
            Utilitaires.checkExtension(file.getOriginalFilename());
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
        getService().getLigneFichierRecouv().saveFile(getService().getStorage().loadAsResource(fichierEnrichiRecouv.getFilename()).getFile(), demandeRecouv);
        changeState(demandeRecouv, Constant.ETATDEM_ATTENTE);
    }
    /**
     * Stockage physique du fichier de la demande sur le disque
     * @param file fichier à sauvegarder issu du client
     * @param fichier objet correspondant au fichier
     * @param demandeRecouv demande rattachée au fichier
     * @return message indiquant le bon déroulement du processus renvoyé au front
     * @throws IOException
     * @throws FileCheckingException
     */
    private String stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeRecouv demandeRecouv) throws IOException, FileCheckingException {
        Integer numDemande = demandeRecouv.getId();
        try {
            getService().getStorage().changePath(Paths.get(uploadPath + numDemande));
            getService().getStorage().init();
            getService().getStorage().store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + numDemande));
            //Ici l'objet fichierRecouv va etre renseigné avec les zones courante et valeur de ces zones
            fichier.checkFileContent(demandeRecouv);
            return Constant.MSG + file.getOriginalFilename() + " a bien été déposé sur le serveur avec le nom "
                    + fichier.getFilename();
        } catch (FileCheckingException e) {
            getService().getStorage().delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    private int getPreviousState(int etatDemande) {
        switch (etatDemande) {
            case Constant.ETATDEM_ATTENTE:
                return Constant.ETATDEM_PREPARATION;
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

    @Override
    public int launchQueryToSudoc(String codeIndex, String valeurs) throws CBSException, QueryToSudocException {
        String[] tabvaleurs = valeurs.split(";");
        String query = getQueryToSudoc(codeIndex, tabvaleurs);
        getService().getTraitement().getCbs().search(query);
        return getService().getTraitement().getCbs().getNbNotices();
    }

    @Override
    public String getInfoFooterFichierResultat(Demande demande) {
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        int nbRechercheTotal = getService().getLigneFichierRecouv().getNbLigneFichierTotalByDemande(demandeRecouv.getId());
        int nbNoticesTrouvees = getDao().getLigneFichierRecouv().getNbReponseTrouveesByDemande(demandeRecouv.getId());
        int nbZeroReponse = getDao().getLigneFichierRecouv().getNbZeroReponseByDemande(demandeRecouv.getId());
        int nbUneReponse = getDao().getLigneFichierRecouv().getNbUneReponseByDemande(demandeRecouv.getId());
        int nbReponseMultiple = getDao().getLigneFichierRecouv().getNbReponseMultipleByDemande(demandeRecouv.getId());

        StringBuilder stringToReturn = new StringBuilder(System.lineSeparator());
        stringToReturn.append(nbNoticesTrouvees).append(" notices trouvées sur ")
                .append(nbRechercheTotal)
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Nb de 1 réponse : ")
                .append(nbUneReponse)
                .append(" | Nb sans réponse : ")
                .append(nbZeroReponse)
                .append(" | Nb plusieurs réponses : ")
                .append(nbReponseMultiple)
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Taux de recouvrement : ")
                .append((nbNoticesTrouvees / nbRechercheTotal) * 100)
                .append("% Taux de création d'exemplaires : ")
                .append((nbUneReponse / nbRechercheTotal) * 100)
                .append("%")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Fin du recouvrement : ")
                .append(new Date());
        return stringToReturn.toString();
    }

    /**
     * Méthode construisant la requête che en fonction des paramètres d'une demande d'exemplarisation
     * @param codeIndex code de l'index de la recherche
     * @param valeur tableau des valeurs utilisées pour construire la requête
     * @return requête che prête à être lancée vers le CBS
     */
    @Override
    public String getQueryToSudoc(String codeIndex, String[] valeur) throws QueryToSudocException {
        switch(codeIndex) {
            case "ISBN":
                return "che isb " + valeur[0];
            case "ISSN":
                return "tno t; tdo t; che isn " + valeur[0];
            case "PPN":
                return "che ppn " + valeur[0];
            case "SOU":
                return "tno t; tdo b; che sou "+ valeur[0];
            case "DAT":
                if(valeur[1].isEmpty()){
                    return "tno t; tdo b; apu " + valeur[0] + "; che mti " + this.replacementOfDiacriticalAccents(valeur[2]);
                }
                return "tno t; tdo b; apu " + valeur[0] + "; che aut " + this.replacementOfDiacriticalAccents(valeur[1]) + " et mti " + this.replacementOfDiacriticalAccents(valeur[2]);
            default :
                throw new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_CODE_NOT_COMPLIANT);
        }
    }

    /**
     * Méthode de remplacement des accents diacritiques
     */
    public String replacementOfDiacriticalAccents(String stringFirst){
        String stringSecond = null;
        stringSecond = stringFirst
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ä", "a")
                .replace("î", "i")
                .replace("ï", "i")
                .replace("ô", "o")
                .replace("ö", "o")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ü", "u")
                .replace("ÿ", "y")
                .replace("æ", "ae")
                .replace("œ", "oe")
                .replace("ç", "c")
                .replace("ñ", "n")
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace("Ë", "E")
                .replace("À", "A")
                .replace("Â", "A")
                .replace("Ä", "A")
                .replace("Î", "I")
                .replace("Ï", "I")
                .replace("Ô", "O")
                .replace("Ö", "O")
                .replace("Ú", "U")
                .replace("Ù", "U")
                .replace("Ü", "U")
                .replace("Û", "U")
                .replace("Ÿ", "Y")
                .replace("Æ", "AE")
                .replace("Œ", "OE")
                .replace("Ç", "C")
                .replace("Ñ", "N");
        return stringSecond;
    }

    /** méthode d'archivage d'une demande
     * supprime les lignes fichiers au moment de l'archivage
     * @param demande demande à archiver
     * @return la demande dans l'état archivé
     * @throws DemandeCheckingException
     */
    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        getService().getLigneFichierRecouv().deleteByDemande(demande);
        return getService().getDemandeRecouv().changeState(demandeRecouv, Constant.ETATDEM_ARCHIVEE);
    }

    @Override
    public List<DemandeRecouv> getIdNextDemandeToArchive() {
        List<DemandeRecouv> listeDemandes;
        listeDemandes = getDao().getDemandeRecouv().getNextDemandeToArchive();
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
        listeDemandes = getDao().getDemandeRecouv().getNextDemandeToPlaceInDeletedStatus();
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
        listeDemandes = getDao().getDemandeRecouv().getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }
}
