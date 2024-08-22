package fr.abes.item.batch.traitement;

import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.service.IDemandeService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;

@Slf4j
public class GetNextDemandeRecouvTasklet  implements Tasklet, StepExecutionListener {
    private final StrategyFactory strategyFactory;
    private DemandeRecouv demande;
    private final int minHour;
    private final int maxHour;

    public GetNextDemandeRecouvTasklet(StrategyFactory factory, int minHour, int maxHour) {
        this.strategyFactory = factory;
        this.minHour = minHour;
        this.maxHour = maxHour;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START_RECOU);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("demandeId", this.demande.getId());
            stepExecution.getJobExecution().getExecutionContext().put("typeDemande", this.demande.getTypeDemande().toString());
        }
        return stepExecution.getExitStatus();
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        log.info(Constant.ENTER_EXECUTE_FROM_GETNEXTDEMANDERECOUVTASKLET);
        try {
            IDemandeService service = strategyFactory.getStrategy(IDemandeService.class, TYPE_DEMANDE.RECOUV);
            this.demande = (DemandeRecouv) service.getIdNextDemandeToProceed(this.minHour, this.maxHour);
            if (this.demande == null) {
                log.info(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            service.changeState(this.demande, Constant.ETATDEM_ENCOURS);
        } catch (DemandeCheckingException e) {
            log.error(Constant.ERROR_PASSERENCOURS_FROM_GETNEXTDEMANDERECOUVTASKLET
                    + e);
            stepContribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        } catch (JDBCConnectionException | ConstraintViolationException j){
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException d){
            log.error("GetNextDemandeRecouvTasklet : Erreur d'accès à la base de donnée");
            if(d.getRootCause() instanceof SQLException sqlEx){
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        return RepeatStatus.FINISHED;
    }
}
