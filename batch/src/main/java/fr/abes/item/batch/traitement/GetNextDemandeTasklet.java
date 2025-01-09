package fr.abes.item.batch.traitement;

import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.util.Objects;

@Slf4j
public class GetNextDemandeTasklet implements Tasklet, StepExecutionListener {
    private final StrategyFactory strategyFactory;
    private Demande demande;
    private TYPE_DEMANDE typeDemande;
    private int minHour;
    private int maxHour;
    private boolean bigVolume = false;
    private JobParameters jobParameters;

    public GetNextDemandeTasklet(StrategyFactory strategyFactory, int minHour, int maxHour, JobParameters jobParameters, TYPE_DEMANDE typeDemande) {
        this.strategyFactory = strategyFactory;
        this.minHour = minHour;
        this.maxHour = maxHour;
        this.typeDemande = typeDemande;
        this.jobParameters = jobParameters;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START + Utilitaires.getLabelTypeDemande(this.typeDemande));
        this.bigVolume = Objects.equals(jobParameters.getString("bigVolume", "false"), "true");
        log.debug("bigVolume : " + this.bigVolume);
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info(Constant.ENTER_EXECUTE_FROM_GETNEXTDEMANDETASKLET);
        try {
            IDemandeService service = strategyFactory.getStrategy(IDemandeService.class, this.typeDemande);
            this.demande = service.getIdNextDemandeToProceed(minHour, maxHour, bigVolume);
            if (this.demande == null) {
                log.info(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            service.changeState(this.demande, Constant.ETATDEM_ENCOURS);
        } catch (JDBCConnectionException | ConstraintViolationException j){
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException d){
            log.error("GetNextDemandeTasklet : Erreur d'accès à la base de donnée");
            if(d.getRootCause() instanceof SQLException sqlEx){
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("demandeId", this.demande.getId());
            stepExecution.getJobExecution().getExecutionContext().put("typeDemande", this.typeDemande);
        }
        return stepExecution.getExitStatus();
    }
}
