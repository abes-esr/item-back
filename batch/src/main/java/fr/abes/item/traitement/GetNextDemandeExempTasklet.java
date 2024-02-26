package fr.abes.item.traitement;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.service.impl.DemandeExempService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;

@Slf4j
public class GetNextDemandeExempTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    DemandeExempService demandeExempService;
    DemandeExemp demande;

    int minHour;

    int maxHour;

    public GetNextDemandeExempTasklet(int minHour, int maxHour) {
        this.minHour = minHour;
        this.maxHour = maxHour;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START_EXEMP);
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
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.warn(Constant.ENTER_EXECUTE_FROM_GETNEXTDEMANDEEXEMPTASKLET);
        try {
            this.demande = demandeExempService.getIdNextDemandeToProceed(this.minHour, this.maxHour);
            if (this.demande == null) {
                log.warn(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            demandeExempService.changeState(this.demande, Constant.ETATDEM_ENCOURS);
            stepContribution.setExitStatus(ExitStatus.COMPLETED);
        } catch (JDBCConnectionException | ConstraintViolationException j){
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DemandeCheckingException e) {
            log.error(Constant.ERROR_PASSERENCOURS_FROM_GETNEXTDEMANDEEXEMPTASKLET
                    + e.toString());
            stepContribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        } catch (DataAccessException d){
            log.error("GetNextDemandeExempTasklet : Erreur d'accès à la base de donnée");
            if(d.getRootCause() instanceof SQLException){
                SQLException sqlEx = (SQLException) d.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        return RepeatStatus.FINISHED;
    }
}
