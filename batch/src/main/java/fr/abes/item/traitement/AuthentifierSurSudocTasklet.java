package fr.abes.item.traitement;

import fr.abes.cbs.exception.CBSException;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.traitement.model.LigneFichierDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Slf4j
public class AuthentifierSurSudocTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    StrategyFactory factory;

    private IMailer mailer;

    private List<LigneFichierDto> lignesFichier;
    @Value("${mail.admin}")
    private String mailAdmin;
    @Autowired
    ProxyRetry proxyRetry;


    private IDemandeService demandeService;

    private String email;
    private Integer demandeId;
    private TYPE_DEMANDE typeDemande;
    private Demande demande;

    private Date dateDebut;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.lignesFichier = (List<LigneFichierDto>) executionContext.get("lignes");
        this.typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        this.demandeId = (Integer) executionContext.get("demandeId");
        demandeService = factory.getStrategy(IDemandeService.class, this.typeDemande);
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

    /**
     * step permettant de s'authentifier au Sudoc
     * @param stepContribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        //récupération du bon type de service demande dans la factory strategy
        mailer.mailDebutTraitement(
                this.email,
                this.demande
        );
        try {
            log.warn("Authentification sudoc sur login M" + this.demande.getRcr());
            this.proxyRetry.authenticate("M" + this.demande.getRcr());
        } catch (CBSException e) {
            log.error(Constant.ERROR_SUDOC_CONNECTION + e.toString());
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
            if(d.getRootCause() instanceof SQLException){
                SQLException sqlEx = (SQLException) d.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        return RepeatStatus.FINISHED;
    }
}
