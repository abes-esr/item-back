package fr.abes.item.batch.traitement;

import fr.abes.cbs.exception.CBSException;
import fr.abes.item.batch.mail.IMailer;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
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
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class AuthentifierSurSudocTasklet implements Tasklet, StepExecutionListener {
    private final StrategyFactory factory;
    private IMailer mailer;
    private final String mailAdmin;
    private final ProxyRetry proxyRetry;
    private IDemandeService demandeService;
    private List<LigneFichierDto> lignesFichier;
    private String email;
    private Demande demande;
    private LocalDateTime dateDebut;

    public AuthentifierSurSudocTasklet(StrategyFactory factory, String mailAdmin, ProxyRetry proxyRetry) {
        this.factory = factory;
        this.mailAdmin = mailAdmin;
        this.proxyRetry = proxyRetry;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.lignesFichier = (List<LigneFichierDto>) executionContext.get("lignes");
        TYPE_DEMANDE typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        Integer demandeId = (Integer) executionContext.get("demandeId");
        demandeService = factory.getStrategy(IDemandeService.class, typeDemande);
        this.demande = demandeService.findById(demandeId);
        this.email = demande.getUtilisateur().getEmail() + ";" + mailAdmin;
        this.mailer = factory.getStrategy(IMailer.class, demande.getTypeDemande());
        this.dateDebut = stepExecution.getJobExecution().getCreateTime();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getJobExecution().getExitStatus().equals(ExitStatus.FAILED)) {
            return ExitStatus.FAILED;
        }
        return ExitStatus.COMPLETED;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        //récupération du bon type de service demande dans la factory strategy
        mailer.mailDebutTraitement(
                this.email,
                this.demande
        );
        try {
            log.warn("Authentification sudoc sur login M" + this.demande.getRcr());
            this.proxyRetry.authenticate("M" + this.demande.getRcr());
        } catch (CBSException e) {
            log.error(Constant.ERROR_SUDOC_CONNECTION + e);
            stepContribution.setExitStatus(ExitStatus.FAILED);
            mailer.mailEchecTraitement(
                    this.email,
                    this.demande,
                    this.dateDebut);
            mailer.mailAlertAdmin(this.mailAdmin, demande);
            Demande d = demandeService.findById(this.lignesFichier.get(0).getRefDemande());
            demandeService.changeState(d, Constant.ETATDEM_ERREUR);
        } catch (DataAccessException d) {
            log.error("AuthentifierSurSudocTasklet : Erreur d'accès à la base de donnée");
            if(d.getRootCause() instanceof SQLException sqlEx){
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        return RepeatStatus.FINISHED;
    }
}
