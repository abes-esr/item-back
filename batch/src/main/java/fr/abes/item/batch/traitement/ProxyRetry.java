package fr.abes.item.batch.traitement;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.batch.traitement.model.ILigneFichierDtoMapper;
import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.batch.traitement.model.LigneFichierDtoModif;
import fr.abes.item.batch.traitement.model.LigneFichierDtoRecouv;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.entities.item.LigneFichierModif;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.service.impl.DemandeExempService;
import fr.abes.item.core.service.impl.DemandeModifService;
import fr.abes.item.core.service.impl.DemandeRecouvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class ProxyRetry {
    private final StrategyFactory factory;
    private final TraitementService traitementService;
    private final DemandeModifService demandeModifService;
    private final DemandeExempService demandeExempService;
    private final DemandeRecouvService demandeRecouvService;


    public ProxyRetry(TraitementService traitementService, StrategyFactory strategyFactory, DemandeModifService demandeModifService, DemandeExempService demandeExempService, DemandeRecouvService demandeRecouvService) {
        this.traitementService = traitementService;
        this.factory = strategyFactory;
        this.demandeModifService = demandeModifService;
        this.demandeExempService = demandeExempService;
        this.demandeRecouvService = demandeRecouvService;
    }

    /**
     * permet de retenter plusieurs fois la connexion à CBS
     *
     * @param login login d'authentification au CBS
     * @throws CBSException erreur de validation CBS
     * @throws IOException erreur de communication avec le CBS
     */
    @Retryable(retryFor = IOException.class, noRetryFor = CBSException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void authenticate(String login) throws CBSException, IOException {
        log.warn(Constant.PROXY_AUTHENTICATION_WITH_LOGIN + login);
        traitementService.authenticate(login);
    }

    @Retryable
    public void disconnect() throws CBSException {
        traitementService.disconnect();
    }
    /**
     * Méthode de modification d'un exemplaire existant dans le CBS (4 tentatives max)
     *
     * @param demande  demande de modification
     * @param ligneFichierDtoModif dto de la ligne fichier à modifier
     * @throws CBSException  : erreur CBS
     * @throws IOException : erreur de communication avec le CBS
     */
    @Retryable(maxAttempts = 4, retryFor = IOException.class, noRetryFor = {CBSException.class, ZoneException.class}, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void saveExemplaire(DemandeModif demande, LigneFichierDtoModif ligneFichierDtoModif) throws CBSException, IOException, ZoneException {
        ILigneFichierDtoMapper ligneFichierDtoMapper = factory.getStrategy(ILigneFichierDtoMapper.class, TYPE_DEMANDE.MODIF);
        try {
            //récupération de la exemplaire correpondant à la ligne du fichier en cours
            String exemplaire = traitementService.getNoticeFromEPN(ligneFichierDtoModif.getEpn());
            //modification de la exemplaire d'exemplaire
            Exemplaire noticeTraitee = demandeModifService.getNoticeTraitee(demande, exemplaire, (LigneFichierModif) ligneFichierDtoMapper.getLigneFichierEntity(ligneFichierDtoModif));
            traitementService.saveExemplaire(noticeTraitee.toString(), ligneFichierDtoModif.getEpn());
        } catch (IOException ex) {
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
     * @throws IOException : erreur de communication avec le CBS
     */
    @Retryable(maxAttempts = 4, retryFor = IOException.class,
            noRetryFor = {CBSException.class, ZoneException.class}, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void newExemplaire(DemandeExemp demande, LigneFichierDtoExemp ligneFichierDtoExemp) throws CBSException, ZoneException, IOException {
        try {
            ligneFichierDtoExemp.setRequete(demandeExempService.getQueryToSudoc(demande.getIndexRecherche().getCode(), demande.getTypeExemp().getLibelle(), ligneFichierDtoExemp.getIndexRecherche().split(";")));
            //lancement de la requête de récupération de la notice dans le CBS
            String numEx = demandeExempService.launchQueryToSudoc(demande, ligneFichierDtoExemp.getIndexRecherche());
            ligneFichierDtoExemp.setNbReponses(demandeExempService.getNbReponses());
            if (ligneFichierDtoExemp.getNbReponses() == 1) {
                ligneFichierDtoExemp.setListePpn(traitementService.getCbs().getPpnEncours());
            } else {
                ligneFichierDtoExemp.setListePpn(traitementService.getCbs().getListePpn().toString());
            }

            String exemplaire = demandeExempService.creerExemplaireFromHeaderEtValeur(demande.getListeZones(), ligneFichierDtoExemp.getValeurZone(), demande.getRcr(), numEx);
            String donneeLocale = demandeExempService.creerDonneesLocalesFromHeaderEtValeur(demande.getListeZones(), ligneFichierDtoExemp.getValeurZone());
            traitementService.getCbs().creerExemplaire(numEx);
            traitementService.getCbs().newExemplaire(exemplaire);
            if (!donneeLocale.isEmpty()) {
                //s'il y a des données locales existantes, on modifie
                if (demandeExempService.hasDonneeLocaleExistante()) {
                    traitementService.getCbs().modLoc(donneeLocale);
                } else {
                    //s'il n'y a pas de donnée locale dans la notice, on crée le bloc
                    traitementService.getCbs().creerDonneeLocale();
                    traitementService.getCbs().newLoc(donneeLocale);
                }
            }
            ligneFichierDtoExemp.setNumExemplaire(numEx);
            ligneFichierDtoExemp.setL035(getL035fromDonneesLocales(donneeLocale));
            ligneFichierDtoExemp.setRetourSudoc(Constant.EXEMPLAIRE_CREE);
        } catch (QueryToSudocException e) {
            ligneFichierDtoExemp.setNbReponses(demandeExempService.getNbReponses());
            ligneFichierDtoExemp.setListePpn(traitementService.getCbs().getListePpn().toString().replace(';', ','));
            ligneFichierDtoExemp.setRetourSudoc("");
        } catch (DataAccessException d) {
            if (d.getRootCause() instanceof SQLException sqlEx) {
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        } catch (IOException ex) {
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
     * @throws IOException : erreur de communication avec le CBS
     */
    @Retryable(maxAttempts = 4, retryFor = IOException.class,
            noRetryFor = {CBSException.class, QueryToSudocException.class}, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void recouvExemplaire(DemandeRecouv demande, LigneFichierDtoRecouv ligneFichierDtoRecouv) throws IOException, QueryToSudocException, CBSException {
        ligneFichierDtoRecouv.setRequete(demandeRecouvService.getQueryToSudoc(demande.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche().split(";")));
        try {
            ligneFichierDtoRecouv.setNbReponses(demandeRecouvService.launchQueryToSudoc(demande.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche()));
            switch (ligneFichierDtoRecouv.getNbReponses()) {
                case 0:
                    ligneFichierDtoRecouv.setListePpn("");
                    break;
                case 1:
                    ligneFichierDtoRecouv.setListePpn(traitementService.getCbs().getPpnEncours());
                    break;
                default:
                    ligneFichierDtoRecouv.setListePpn(traitementService.getCbs().getListePpn().toString().replace(';', ','));
            }
        } catch (IOException ex) {
            log.error("Erreur de communication avec le CBS sur demande recouv " + demande.getId() + " / ligne fichier n°" + ligneFichierDtoRecouv.getNumLigneFichier());
            //si un pb de communication avec le CBS est détecté, on se reconnecte, et on renvoie l'exception pour que le retry retente la méthode
            this.disconnect();
            this.authenticate("M" + demande.getRcr());
            throw ex;
        }
    }

}
