package fr.abes.item.batch.traitement;

import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.service.FileSystemStorageService;
import fr.abes.item.core.service.IDemandeService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class DeleteDemandesRecouvTasklet implements Tasklet, StepExecutionListener {
    private final StrategyFactory strategyFactory;
    private final FileSystemStorageService storageService;
    private final String uploadPath;
    List<DemandeRecouv> demandes;

    public DeleteDemandesRecouvTasklet(StrategyFactory strategyFactory, FileSystemStorageService storageService, String uploadPath) {
        this.strategyFactory = strategyFactory;
        this.storageService = storageService;
        this.uploadPath = uploadPath;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {}

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        IDemandeService service = strategyFactory.getStrategy(IDemandeService.class, TYPE_DEMANDE.RECOUV);
        this.demandes = (List<DemandeRecouv>) service.getIdNextDemandeToDelete();
        if (this.demandes == null) {
            log.info(Constant.NO_DEMANDE_TO_PROCESS);
            stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
            return RepeatStatus.FINISHED;
        }
        //Iteration sur chaque demande pour en modifier le statut
        for (Demande demande : this.demandes) {
            log.info("Suppression définitive de la demande de recouvrement " + demande.getNumDemande());
            service.deleteById(demande.getId());
            storageService.changePath(Paths.get(uploadPath + "recouv/" + demande.getId()));
            storageService.deleteAll();
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
