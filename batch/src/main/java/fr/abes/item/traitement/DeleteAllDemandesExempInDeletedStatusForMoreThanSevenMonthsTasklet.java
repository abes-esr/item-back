package fr.abes.item.traitement;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.service.service.ServiceProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

@Slf4j
public class DeleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    @Getter
    ServiceProvider service;

    List<DemandeExemp> demandes;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Suppression déifnitive des demandes en base d'exemplarisation, modification et recouvrement");
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.warn("entrée dans execute de DeleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet...");
        this.demandes = getService().getDemandeExemp().getIdNextDemandeToDelete();
        if (this.demandes == null) {
            log.warn(Constant.NO_DEMANDE_TO_PROCESS);
            stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
            return RepeatStatus.FINISHED;
        }
        //Iteration sur chaque demande pour en modifier le statut
        Iterator<DemandeExemp> it = this.demandes.iterator();
        while (it.hasNext()) {
            Demande demande = it.next();
            log.info("Suppression définitive de la demande d'exemplarisation " + demande.getNumDemande());
            getService().getDemandeExemp().deleteById(demande.getId());
        }
        stepContribution.setExitStatus(ExitStatus.COMPLETED);
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("demandes", this.demandes);
        }
        return stepExecution.getExitStatus();
    }
}
