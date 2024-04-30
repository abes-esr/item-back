package fr.abes.item.batch.traitement.traiterlignesfichierchunk;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.batch.traitement.ProxyRetry;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.batch.traitement.model.LigneFichierDtoModif;
import fr.abes.item.batch.traitement.model.LigneFichierDtoRecouv;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.service.IDemandeService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class LignesFichierProcessor implements ItemProcessor<LigneFichierDto, LigneFichierDto>, StepExecutionListener {
    private final StrategyFactory strategyFactory;
    private final ProxyRetry proxyRetry;

    private Demande demande;

    public LignesFichierProcessor(StrategyFactory strategyFactory, ProxyRetry proxyRetry) {
        this.strategyFactory = strategyFactory;
        this.proxyRetry = proxyRetry;
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        TYPE_DEMANDE typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        IDemandeService demandeService = strategyFactory.getStrategy(IDemandeService.class, typeDemande);
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
        } catch (CBSException | ZoneException | QueryToSudocException | IOException e) {
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
     * @throws CBSException  : erreur CBS
     * @throws ZoneException : erreur de construction de la notice
     * @throws IOException : erreur de communication avec le CBS
     */
    private LigneFichierDtoModif processDemandeModif(LigneFichierDto ligneFichierDto) throws CBSException, ZoneException, IOException {
        DemandeModif demandeModif = (DemandeModif) demande;
        LigneFichierDtoModif ligneFichierDtoModif = (LigneFichierDtoModif) ligneFichierDto;
        //sauvegarde la notice modifiée
        this.proxyRetry.saveExemplaire(demandeModif, ligneFichierDtoModif);
        ligneFichierDtoModif.setRetourSudoc(Constant.EXEMPLAIRE_MODIFIE);
        return ligneFichierDtoModif;
    }

    /**
     * Méthode permettant de lancer l'exemplarisation sur une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer le traitement d'exemplarisation
     * @return la DTO de la ligne fichier modifiée en fonction du résultat du traitement
     * @throws CBSException  : erreur CBS
     * @throws ZoneException : erreur de construction de la notice
     * @throws IOException : erreur de communication avec le CBS
     */
    private LigneFichierDtoExemp processDemandeExemp(LigneFichierDto ligneFichierDto) throws CBSException, ZoneException, IOException {
        DemandeExemp demandeExemp = (DemandeExemp) this.demande;
        LigneFichierDtoExemp ligneFichierDtoExemp = (LigneFichierDtoExemp) ligneFichierDto;
        this.proxyRetry.newExemplaire(demandeExemp, ligneFichierDtoExemp);
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
     * @throws CBSException          : erreur CBS
     * @throws QueryToSudocException : erreur dans le type d'index de recherche
     * @throws IOException         : erreur de communication avec le CBS
     */
    private LigneFichierDtoRecouv processDemandeRecouv(LigneFichierDto ligneFichierDto) throws CBSException, QueryToSudocException, IOException {
        DemandeRecouv demandeRecouv = (DemandeRecouv) this.demande;
        LigneFichierDtoRecouv ligneFichierDtoRecouv = (LigneFichierDtoRecouv) ligneFichierDto;
        this.proxyRetry.recouvExemplaire(demandeRecouv, ligneFichierDtoRecouv);
        return ligneFichierDtoRecouv;
    }
}
