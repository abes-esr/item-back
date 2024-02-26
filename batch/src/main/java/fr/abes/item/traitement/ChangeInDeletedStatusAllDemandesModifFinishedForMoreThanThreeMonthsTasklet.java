package fr.abes.item.traitement;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.service.impl.DemandeModifService;
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
public class ChangeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    DemandeModifService demandeModifService;

    List<DemandeModif> demandes;

    @Override
    public void beforeStep(StepExecution stepExecution) {}

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.warn("entrée dans execute de ChangeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet");
        try {
            this.demandes = demandeModifService.getIdNextDemandeToPlaceInDeletedStatus();
            if (this.demandes == null) {
                log.warn(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            //Iteration sur chaque demande pour en modifier le statut
            Iterator<DemandeModif> it = this.demandes.iterator();
            while (it.hasNext()) {
                Demande demande = it.next();
                log.info("Passage de la demande d'exemplarisation " + demande.getNumDemande() + "au statut" + Constant.ETATDEM_SUPPRIMEE);
                demandeModifService.changeState(demande, Constant.ETATDEM_SUPPRIMEE);
            }
            stepContribution.setExitStatus(ExitStatus.COMPLETED);
        } catch (DemandeCheckingException e) {
            log.error("Erreur lors du passage à statut supprimé de ChangeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet"
                    + e.toString());
            stepContribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }
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
