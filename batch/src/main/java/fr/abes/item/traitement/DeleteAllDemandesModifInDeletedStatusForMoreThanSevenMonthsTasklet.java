package fr.abes.item.traitement;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.service.impl.DemandeModifService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class DeleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    private DemandeModifService demandeModifService;

    List<DemandeModif> demandes;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {}

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        log.warn("entrée dans execute de DeleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet...");
        this.demandes = demandeModifService.getIdNextDemandeToDelete();
        if (this.demandes == null) {
            log.warn(Constant.NO_DEMANDE_TO_PROCESS);
            stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
            return RepeatStatus.FINISHED;
        }
        //Iteration sur chaque demande pour en modifier le statut
        for (Demande demande : this.demandes) {
            log.info("Suppression définitive de la demande de modification " + demande.getNumDemande());
            demandeModifService.deleteById(demande.getId());
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
