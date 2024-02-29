package fr.abes.item.traitement.traiterlignesfichierchunk;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.QueryToSudocException;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.TraitementService;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.service.impl.DemandeExempService;
import fr.abes.item.service.impl.DemandeModifService;
import fr.abes.item.service.impl.DemandeRecouvService;
import fr.abes.item.traitement.ProxyRetry;
import fr.abes.item.traitement.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class LignesFichierProcessor implements ItemProcessor<LigneFichierDto, LigneFichierDto>, StepExecutionListener {
    @Autowired
    StrategyFactory factory;
    @Autowired
    ProxyRetry proxyRetry;
    @Autowired
    TraitementService traitementService;

    private Demande demande;
    
    @Autowired
    private DemandeModifService demandeModifService;

    @Autowired
    private DemandeExempService demandeExempService;
    
    @Autowired
    private DemandeRecouvService demandeRecouvService;
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        TYPE_DEMANDE typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        IDemandeService demandeService = factory.getStrategy(IDemandeService.class, typeDemande);
        Integer demandeId = (Integer) executionContext.get("demandeId");
        this.demande = demandeService.findById(demandeId);
        log.info(Constant.POUR_LA_DEMANDE + this.demande.getNumDemande());
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return null;
    }

    @Override
    public LigneFichierDto process(@NonNull LigneFichierDto ligneFichierDto) {
        try {
            return switch (ligneFichierDto.getTypeDemande()) {
                case MODIF -> processDemandeModif(ligneFichierDto);
                case EXEMP -> processDemandeExemp(ligneFichierDto);
                default -> processDemandeRecouv(ligneFichierDto);
            };
        } catch (CBSException e) {
            log.error(Constant.ERROR_FROM_SUDOC_REQUEST_OR_METHOD_SAVEXEMPLAIRE + e);
            ligneFichierDto.setRetourSudoc(e.getMessage());
        } catch (JDBCConnectionException | ConstraintViolationException j) {
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException d) {
            if (d.getRootCause() instanceof SQLException sqlEx) {
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        } catch (Exception e) {
            log.error(Constant.ERROR_FROM_RECUP_NOTICETRAITEE + e);
            ligneFichierDto.setRetourSudoc(e.getMessage());
        }
        return ligneFichierDto;
    }

    /**
     * Méthode permettant de lancer la modification sur une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer le traitement de modification
     * @return la DTO de la ligne fichier modifiée en fonction du résultat du traitement
     * @throws CBSException : erreur d'accès au Sudoc
     */
    private LigneFichierDtoModif processDemandeModif(LigneFichierDto ligneFichierDto) throws CBSException, ZoneException {
        DemandeModif demandeModif = (DemandeModif) demande;
        ILigneFichierDtoMapper ligneFichierDtoMapper = factory.getStrategy(ILigneFichierDtoMapper.class, TYPE_DEMANDE.MODIF);
        LigneFichierDtoModif ligneFichierDtoModif = (LigneFichierDtoModif) ligneFichierDto;
        //récupération de la notice correpondant à la ligne du fichier en cours
        String notice = traitementService.getNoticeFromEPN(ligneFichierDtoModif.getEpn());
        //modification de la notice d'exemplaire
        String noticetraitee = demandeModifService.getNoticeTraitee(demandeModif, notice, (LigneFichierModif) ligneFichierDtoMapper.getLigneFichierEntity(ligneFichierDtoModif));
        //sauvegarde la notice modifiée
        this.proxyRetry.saveExemplaire(noticetraitee);
        ligneFichierDtoModif.setRetourSudoc(Constant.EXEMPLAIRE_MODIFIE);
        return ligneFichierDtoModif;
    }

    /**
     * Méthode permettant de lancer l'exemplarisation sur une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer le traitement d'exemplarisation
     * @return la DTO de la ligne fichier modifiée en fonction du résultat du traitement
     * @throws CBSException : erreur d'accès au Sudoc
     * @throws QueryToSudocException 0 ou plus de 1 résultat à la requête che
     */
    private LigneFichierDtoExemp processDemandeExemp(LigneFichierDto ligneFichierDto) throws Exception {
        DemandeExemp demandeExemp = (DemandeExemp) this.demande;

        LigneFichierDtoExemp ligneFichierDtoExemp = (LigneFichierDtoExemp) ligneFichierDto;
        try {
            ligneFichierDtoExemp.setRequete(demandeExempService.getQueryToSudoc(demandeExemp.getIndexRecherche().getCode(), demandeExemp.getTypeExemp().getLibelle(), ligneFichierDtoExemp.getIndexRecherche().split(";")));
            //lancement de la requête de récupération de la notice dans le CBS
            String numEx = demandeExempService.launchQueryToSudoc(demandeExemp, ligneFichierDtoExemp.getIndexRecherche());
            ligneFichierDtoExemp.setNbReponses(demandeExempService.getNbReponses());
            if (ligneFichierDtoExemp.getNbReponses() == 1) {
                ligneFichierDtoExemp.setListePpn(traitementService.getCbs().getPpnEncours());
            } else {
                ligneFichierDtoExemp.setListePpn(traitementService.getCbs().getListePpn().toString());
            }

            String exemplaire = demandeExempService.creerExemplaireFromHeaderEtValeur(demandeExemp.getListeZones(), ligneFichierDtoExemp.getValeurZone(), demandeExemp.getRcr(), numEx);
            String donneeLocale = demandeExempService.creerDonneesLocalesFromHeaderEtValeur(demandeExemp.getListeZones(), ligneFichierDtoExemp.getValeurZone());

            this.proxyRetry.newExemplaire(numEx, exemplaire, donneeLocale, demandeExempService.hasDonneeLocaleExistante());
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
        }
        return ligneFichierDtoExemp;
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
     * Méthode permettant de lancer le test de recouvrement pour une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer la requête
     * @return la DTO ligneFichier mise à jour en fonction du résultat de la requête che
     * @throws CBSException : erreur d'accès au Sudoc
     */
    private LigneFichierDtoRecouv processDemandeRecouv(LigneFichierDto ligneFichierDto) throws CBSException, QueryToSudocException {
        DemandeRecouv demandeRecouv = (DemandeRecouv) this.demande;
        LigneFichierDtoRecouv ligneFichierDtoRecouv = (LigneFichierDtoRecouv) ligneFichierDto;
        ligneFichierDtoRecouv.setRequete(demandeRecouvService.getQueryToSudoc(demandeRecouv.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche().split(";")));
        ligneFichierDtoRecouv.setNbReponses(demandeRecouvService.launchQueryToSudoc(demandeRecouv.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche()));
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

        return ligneFichierDtoRecouv;
    }
}
