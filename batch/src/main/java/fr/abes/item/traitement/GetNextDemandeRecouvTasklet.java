package fr.abes.item.traitement;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.service.service.ServiceProvider;
import lombok.Getter;
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
public class GetNextDemandeRecouvTasklet  implements Tasklet, StepExecutionListener {
    @Getter
    @Autowired
    ServiceProvider service;

    private DemandeRecouv demande;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START_RECOU);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("demande", this.demande);
        }
        return stepExecution.getExitStatus();
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.warn(Constant.ENTER_EXECUTE_FROM_GETNEXTDEMANDERECOUVTASKLET);
        try {
            this.demande = (DemandeRecouv) getService().getDemandeRecouv().getIdNextDemandeToProceed();
            if (this.demande == null) {
                log.warn(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            getService().getDemandeRecouv().changeState(this.demande, Constant.ETATDEM_ENCOURS);
        } catch (DemandeCheckingException e) {
            log.error(Constant.ERROR_PASSERENCOURS_FROM_GETNEXTDEMANDERECOUVTASKLET
                    + e.toString());
            stepContribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        } catch (JDBCConnectionException | ConstraintViolationException j){
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException d){
            log.error("GetNextDemandeRecouvTasklet : Erreur d'accès à la base de donnée");
            if(d.getRootCause() instanceof SQLException){
                SQLException sqlEx = (SQLException) d.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        return RepeatStatus.FINISHED;
    }
}
