package fr.abes.item.traitement;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.CommException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.entities.item.LigneFichierModif;
import fr.abes.item.exception.QueryToSudocException;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.service.service.ServiceProvider;
import fr.abes.item.traitement.model.ILigneFichierDtoMapper;
import fr.abes.item.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.traitement.model.LigneFichierDtoModif;
import fr.abes.item.traitement.model.LigneFichierDtoRecouv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class ProxyRetry {
    @Autowired
    @Getter
    private ServiceProvider service;

    @Autowired
    private StrategyFactory factory;

    /**
     * permet de retenter plusieurs fois la connexion à CBS
     *
     * @param login login d'authentification
     * @throws CBSException erreur de validation CBS
     * @throws CommException erreur de communication avec le CBS
     */
    @Retryable(include = CommException.class, exclude = CBSException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void authenticate(String login) throws CBSException, CommException {
        log.warn(Constant.PROXY_AUTHENTICATION_WITH_LOGIN + login);
        getService().getTraitement().authenticate(login);
    }

    @Retryable
    public void disconnect() throws CBSException {
        getService().getTraitement().disconnect();
    }
    /**
     * Méthode de modification d'un exemplaire existant dans le CBS (4 tentatives max)
     *
     * @param demande  demande de modification
     * @param ligneFichierDtoModif dto de la ligne fichier à modifier
     * @throws CBSException  : erreur CBS
     * @throws CommException : erreur de communication avec le CBS
     */
    @Retryable(maxAttempts = 4, include = CommException.class, exclude = {CBSException.class, ZoneException.class}, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void saveExemplaire(DemandeModif demande, LigneFichierDtoModif ligneFichierDtoModif) throws CBSException, CommException, ZoneException {
        ILigneFichierDtoMapper ligneFichierDtoMapper = factory.getStrategy(ILigneFichierDtoMapper.class, TYPE_DEMANDE.MODIF);
        try {
            //récupération de la exemplaire correpondant à la ligne du fichier en cours
            String exemplaire = getService().getTraitement().getNoticeFromEPN(ligneFichierDtoModif.getEpn());
            //modification de la exemplaire d'exemplaire
            Exemplaire noticeTraitee = getService().getDemandeModif().getNoticeTraitee(demande, exemplaire, (LigneFichierModif) ligneFichierDtoMapper.getLigneFichierEntity(ligneFichierDtoModif));
            getService().getTraitement().saveExemplaire(noticeTraitee.toString(), ligneFichierDtoModif.getEpn());
        } catch (CBSException ex) {
            //en cas d'erreur CBS de type Fatal (erreur qui ne devrait pas se produire) on se déconnecte / reconnecte et on renvoie l'exception
            if (ex.getCodeErreur().equals(Level.FATAL)) {
                this.disconnect();
                this.authenticate("M" + demande.getRcr());
            }
            throw ex;
        } catch (CommException ex) {
            log.error("Erreur de communication avec le CBS sur demande modif " + demande.getId() + " / ligne fichier n°" + ligneFichierDtoModif.getNumLigneFichier() + " / epn : " + ligneFichierDtoModif.getEpn());
            //si un pb de communication avec le CBS est détecté, on se reconnecte, et on renvoie l'exception pour que le retry retente la méthode
            this.disconnect();
            this.authenticate("M" + demande.getRcr());
            throw ex;
        }
    }

    /**
     * Méthode permettant la création d'un nouvel exemplaire et du bloc de donnée locale dans le CBS (4 tentatives max)
     * @param demande demande d'exemplarisation à traiter
     * @param ligneFichierDtoExemp ligne fichier à traiter
     * @throws CBSException : erreur CBS
     * @throws ZoneException : erreur de construction de la notice
     * @throws CommException : erreur de communication avec le CBS
     */
    @Retryable(maxAttempts = 4, include = CommException.class,
            exclude = {CBSException.class, ZoneException.class}, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void newExemplaire(DemandeExemp demande, LigneFichierDtoExemp ligneFichierDtoExemp) throws CBSException, ZoneException, CommException {
        try {
            ligneFichierDtoExemp.setRequete(getService().getDemandeExemp().getQueryToSudoc(demande.getIndexRecherche().getCode(), demande.getTypeExemp().getLibelle(), ligneFichierDtoExemp.getIndexRecherche().split(";")));
            //lancement de la requête de récupération de la notice dans le CBS
            String numEx = getService().getDemandeExemp().launchQueryToSudoc(demande, ligneFichierDtoExemp.getIndexRecherche());
            ligneFichierDtoExemp.setNbReponses(getService().getDemandeExemp().getNbReponses());
            if (ligneFichierDtoExemp.getNbReponses() == 1) {
                ligneFichierDtoExemp.setListePpn(getService().getTraitement().getCbs().getPpnEncours());
            } else {
                ligneFichierDtoExemp.setListePpn(getService().getTraitement().getCbs().getListePpn().toString());
            }

            String exemplaire = getService().getDemandeExemp().creerExemplaireFromHeaderEtValeur(demande.getListeZones(), ligneFichierDtoExemp.getValeurZone(), demande.getRcr(), numEx);
            String donneeLocale = getService().getDemandeExemp().creerDonneesLocalesFromHeaderEtValeur(demande.getListeZones(), ligneFichierDtoExemp.getValeurZone());
            getService().getTraitement().getCbs().creerExemplaire(numEx);
            getService().getTraitement().getCbs().newExemplaire(exemplaire);
            if (!donneeLocale.isEmpty()) {
                //s'il y a des données locales existantes, on modifie
                if (getService().getDemandeExemp().hasDonneeLocaleExistante()) {
                    getService().getTraitement().getCbs().modLoc(donneeLocale);
                } else {
                    //s'il n'y a pas de donnée locale dans la notice, on crée le bloc
                    getService().getTraitement().getCbs().creerDonneeLocale();
                    getService().getTraitement().getCbs().newLoc(donneeLocale);
                }
            }
            ligneFichierDtoExemp.setNumExemplaire(numEx);
            ligneFichierDtoExemp.setL035(getL035fromDonneesLocales(donneeLocale));
            ligneFichierDtoExemp.setRetourSudoc(Constant.EXEMPLAIRE_CREE);
        } catch (QueryToSudocException e) {
            ligneFichierDtoExemp.setNbReponses(getService().getDemandeExemp().getNbReponses());
            ligneFichierDtoExemp.setListePpn(getService().getTraitement().getCbs().getListePpn().toString().replace(';', ','));
            ligneFichierDtoExemp.setRetourSudoc("");
        } catch (DataAccessException d) {
            if (d.getRootCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) d.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        } catch (CommException ex) {
            log.error("Erreur de communication avec le CBS sur demande exemp " + demande.getId() + " / ligne fichier n°" + ligneFichierDtoExemp.getNumLigneFichier());
            //si un pb de communication avec le CBS est détecté, on se reconnecte, et on renvoie l'exception pour que le retry retente la méthode
            this.disconnect();
            this.authenticate("M" + demande.getRcr());
            throw ex;
        }
    }

    private String getL035fromDonneesLocales(String donneeLocale) throws ZoneException {
        DonneeLocale donneeLocale1 = new DonneeLocale(donneeLocale);
        List<Zone> listeL035 = donneeLocale1.findZones("L035");
        if (!listeL035.isEmpty()) {
            return listeL035.get(listeL035.size() - 1).findSubLabel("$a");
        }
        return null;
    }

    /**
     * Méthode permettant de traiter une ligne d'une demande de recouvrement
     * @param demande demande de recouvrement à traiter
     * @param ligneFichierDtoRecouv ligne fichier à traiter
     * @throws CBSException : erreur CBS
     * @throws QueryToSudocException : erreur dans le type d'index de recherche
     * @throws CommException : erreur de communication avec le CBS
     */
    @Retryable(maxAttempts = 4, include = CommException.class,
            exclude = {CBSException.class, QueryToSudocException.class}, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void recouvExemplaire(DemandeRecouv demande, LigneFichierDtoRecouv ligneFichierDtoRecouv) throws CommException, QueryToSudocException, CBSException {
        ligneFichierDtoRecouv.setRequete(getService().getDemandeRecouv().getQueryToSudoc(demande.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche().split(";")));
        try {
            ligneFichierDtoRecouv.setNbReponses(getService().getDemandeRecouv().launchQueryToSudoc(demande.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche()));
            switch (ligneFichierDtoRecouv.getNbReponses()) {
                case 0:
                    ligneFichierDtoRecouv.setListePpn("");
                    break;
                case 1:
                    ligneFichierDtoRecouv.setListePpn(getService().getTraitement().getCbs().getPpnEncours());
                    break;
                default:
                    ligneFichierDtoRecouv.setListePpn(getService().getTraitement().getCbs().getListePpn().toString().replace(';', ','));
            }
        } catch (CommException ex) {
            log.error("Erreur de communication avec le CBS sur demande recouv " + demande.getId() + " / ligne fichier n°" + ligneFichierDtoRecouv.getNumLigneFichier());
            //si un pb de communication avec le CBS est détecté, on se reconnecte, et on renvoie l'exception pour que le retry retente la méthode
            this.disconnect();
            this.authenticate("M" + demande.getRcr());
            throw ex;
        }
    }

}
