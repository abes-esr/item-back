package fr.abes.item.traitement.traiterlignesfichierchunk;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.ILigneFichier;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.exception.FileLineException;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.traitement.model.ILigneFichierDtoMapper;
import fr.abes.item.traitement.model.LigneFichierDto;
import fr.abes.item.exception.DemandeCheckingException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class LignesFichierWriter implements ItemWriter<LigneFichierDto>, StepExecutionListener {
    @Autowired
    private StrategyFactory factory;

    private IMailer mailer;
    private ILigneFichierService ligneFichierService;
    private IDemandeService demandeService;
    private List<LigneFichierDto> lignesFichier;
    private Demande demande;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.lignesFichier = (List<LigneFichierDto>) executionContext.get("lignes");
        this.demande = (Demande) executionContext.get("demande");
        this.ligneFichierService = factory.getStrategy(ILigneFichierService.class, demande.getTypeDemande());
        this.demandeService = factory.getStrategy(IDemandeService.class, demande.getTypeDemande());
        this.mailer = factory.getStrategy(IMailer.class, demande.getTypeDemande());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            demandeService.closeDemande(this.demande);
        } catch (DataAccessException d){
            if(d.getRootCause() instanceof SQLException){
                SQLException sqlEx = (SQLException) d.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        catch (DemandeCheckingException e) {
            log.error(Constant.ERROR_TREATMENT_LIGNE_FICHIER_WHEN_UPDATE_DEMANDE_STATE
                    + e.toString());
            return ExitStatus.FAILED;
        }
        stepExecution.getJobExecution().getExecutionContext().put("lignes", this.lignesFichier);
        return ExitStatus.COMPLETED;
    }

    @Override
    public void write(List<? extends LigneFichierDto> liste) {
        for (LigneFichierDto ligneFichierDto : liste) {
            try {
                this.majLigneFichier(ligneFichierDto);
                this.majPourcentageTraitementDemande();
            } catch (DataAccessException d){
                if(d.getRootCause() instanceof SQLException){
                    SQLException sqlEx = (SQLException) d.getRootCause();
                    log.error("Erreur SQL : " + sqlEx.getErrorCode());
                    log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
                }
            } catch (Exception e) {
                log.error(Constant.ERROR_MAJ_LIGNE_FICHIER_WRITE + e.toString());
            }
        }
    }

    private void majPourcentageTraitementDemande(){
        Integer percent = (int) Math.round((double)this.ligneFichierService.getNbLigneFichierTraiteeByDemande(demande.getNumDemande()) / (double)this.ligneFichierService.getNbLigneFichierTotalByDemande(demande.getNumDemande())*100);
        demande.setPourcentageProgressionTraitement(percent);
        demandeService.save(demande);
    }

    private void majLigneFichier(LigneFichierDto item) throws FileLineException {
        try {
            ILigneFichier ligneFichier = (ILigneFichier) ligneFichierService.findById(item.getNumLigneFichier());
            ILigneFichierDtoMapper ligneFichierDtoMapper = factory.getStrategy(ILigneFichierDtoMapper.class, demande.getTypeDemande());
            ligneFichier.setEntityAfterBatch(ligneFichierDtoMapper.getLigneFichierEntity(item));
            ligneFichierService.save((LigneFichier)ligneFichier);
            log.info(Constant.LIGNE_TRAITEE + item.getNumLigneFichier());
        } catch (JDBCConnectionException | ConstraintViolationException j){
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException e) {
            if(e.getRootCause() instanceof SQLException){
                SQLException sqlEx = (SQLException) e.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
            log.error(Constant.ERROR_MAJ_LIGNE + item.getNumLigneFichier()
                    + " pour la demande " + item.getRefDemande()
                    + " "
                    + e.toString());
            throw new FileLineException(Constant.ERR_FILE_LINEFILE);
        }
    }


}
