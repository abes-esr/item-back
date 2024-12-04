package fr.abes.item.batch.traitement.traiterlignesfichierchunk;

import fr.abes.item.batch.traitement.model.ILigneFichierDtoMapper;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.ILigneFichier;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileLineException;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ILigneFichierService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class LignesFichierWriter implements ItemWriter<LigneFichierDto>, StepExecutionListener {
    private final StrategyFactory factory;
    private final EntityManager itemEntityManager;
    private ILigneFichierService ligneFichierService;
    private IDemandeService demandeService;
    private List<LigneFichierDto> lignesFichier;
    private Demande demande;
    private Integer demandeId;

    public LignesFichierWriter(StrategyFactory factory, EntityManager itemEntityManager) {
        this.itemEntityManager = itemEntityManager;
        this.factory = factory;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.lignesFichier = (List<LigneFichierDto>) executionContext.get("lignes");
        TYPE_DEMANDE typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        this.demandeService = factory.getStrategy(IDemandeService.class, typeDemande);
        this.demandeId = (Integer) executionContext.get("demandeId");
        this.demande = demandeService.findById(this.demandeId);
        this.ligneFichierService = factory.getStrategy(ILigneFichierService.class, demande.getTypeDemande());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            this.demande = demandeService.findById(demandeId);
            itemEntityManager.detach(this.demande);
            if(demande.getEtatDemande().getId() != Constant.ETATDEM_INTEROMPU) {
                demandeService.closeDemande(this.demande);
            }
        } catch (DataAccessException d){
            if(d.getRootCause() instanceof SQLException sqlEx){
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        }
        catch (DemandeCheckingException e) {
            log.error(Constant.ERROR_TREATMENT_LIGNE_FICHIER_WHEN_UPDATE_DEMANDE_STATE + e);
            return ExitStatus.FAILED;
        }
        stepExecution.getJobExecution().getExecutionContext().put("lignes", this.lignesFichier);
        return ExitStatus.COMPLETED;
    }

    @Override
    public void write(Chunk<? extends LigneFichierDto> liste) {
        for (LigneFichierDto ligneFichierDto : liste) {
            try {
                this.demande = demandeService.findById(demandeId);

                this.majLigneFichier(ligneFichierDto);
                this.majPourcentageTraitementDemande();
            } catch (DataAccessException d){
                if(d.getRootCause() instanceof SQLException sqlEx){
                    log.error("Erreur SQL : " + sqlEx.getErrorCode());
                    log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
                }
            } catch (Exception e) {
                log.error(Constant.ERROR_MAJ_LIGNE_FICHIER_WRITE + e);
            }
        }
    }

    private void majPourcentageTraitementDemande(){
        Integer percent = (int) Math.round((double)this.ligneFichierService.getNbLigneFichierTraiteeByDemande(demande) / (double)this.ligneFichierService.getNbLigneFichierTotalByDemande(demande)*100);
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
            if(e.getRootCause() instanceof SQLException sqlEx){
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
            log.error(Constant.ERROR_MAJ_LIGNE + item.getNumLigneFichier()
                    + " pour la demande " + item.getRefDemande()
                    + " "
                    + e);
            throw new FileLineException(Constant.ERR_FILE_LINEFILE);
        }
    }
}
