package fr.abes.item.batch.traitement;

import fr.abes.item.core.components.FichierSauvegardeSupp;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.service.IDemandeService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Paths;

@Slf4j
public class CreerFichierSauvegardeTasklet implements Tasklet, StepExecutionListener {
    private final StrategyFactory factory;
    private final String uploadPath;
    private Demande demande;
    private FichierSauvegardeSupp fichier;

    public CreerFichierSauvegardeTasklet(StrategyFactory factory, String uploadPath) {
        this.factory = factory;
        this.uploadPath = uploadPath;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        TYPE_DEMANDE typeDemande = (TYPE_DEMANDE) executionContext.get("typeDemande");
        IDemandeService demandeService = factory.getStrategy(IDemandeService.class, typeDemande);
        Integer demandeId = (Integer) executionContext.get("demandeId");
        this.demande = demandeService.findById(demandeId);

    }
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        this.fichier = (FichierSauvegardeSupp) FichierFactory.getFichier(Constant.ETATDEM_ATTENTE, TYPE_DEMANDE.SUPP);
        fichier.generateFileName(this.demande);
        fichier.setPath(Paths.get(uploadPath + demande.getTypeDemande().toString().toLowerCase() + "/" +  demande.getId()));
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("fichierTxtPath", this.fichier.getPath().toString());
            stepExecution.getJobExecution().getExecutionContext().put("fichierTxtName", this.fichier.getFilename());
        }
        return stepExecution.getExitStatus();
    }
}
