package fr.abes.item.traitement.traiterlignesfichierchunk;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.exception.QueryToSudocException;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.service.service.ServiceProvider;
import fr.abes.item.traitement.ProxyRetry;
import fr.abes.item.traitement.model.LigneFichierDto;
import fr.abes.item.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.traitement.model.LigneFichierDtoModif;
import fr.abes.item.traitement.model.LigneFichierDtoRecouv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
@Component
public class LignesFichierProcessor implements ItemProcessor<LigneFichierDto, LigneFichierDto>, StepExecutionListener {
    @Autowired
    StrategyFactory factory;
    @Autowired
    ProxyRetry proxyRetry;

    @Autowired
    @Getter
    private ServiceProvider service;

    private IDemandeService demandeService;

    private Integer demandeId;
    private TYPE_DEMANDE typeDemande;
    private Demande demande;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        demandeService = factory.getStrategy(IDemandeService.class, this.typeDemande);
        this.demandeId = (Integer) executionContext.get("demandeId");
        this.demande = demandeService.findById(this.demandeId);
        log.info(Constant.POUR_LA_DEMANDE + this.demande.getNumDemande());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public LigneFichierDto process(LigneFichierDto ligneFichierDto) {
        try {
            switch (ligneFichierDto.getTypeDemande()) {
                case MODIF:
                    return processDemandeModif(ligneFichierDto);
                case EXEMP:
                    return processDemandeExemp(ligneFichierDto);
                default:
                    return processDemandeRecouv(ligneFichierDto);
            }
        } catch (CBSException | ZoneException | QueryToSudocException | IOException e) {
            log.error(Constant.ERROR_FROM_SUDOC_REQUEST_OR_METHOD_SAVEXEMPLAIRE + e);
            ligneFichierDto.setRetourSudoc(e.getMessage());
        } catch (JDBCConnectionException | ConstraintViolationException j) {
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException d) {
            if (d.getRootCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) d.getRootCause();
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        } catch (Exception e) {
            log.error(Constant.ERROR_FROM_RECUP_NOTICETRAITEE + e);
            ligneFichierDto.setRetourSudoc(e.getMessage());
        }
        return ligneFichierDto;
    }

    /**
     * Méthode permettant de lancer la modification sur une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer le traitement de modification
     * @return la DTO de la ligne fichier modifiée en fonction du résultat du traitement
     * @throws CBSException  : erreur CBS
     * @throws ZoneException : erreur de construction de la notice
     * @throws IOException : erreur de communication avec le CBS
     */
    private LigneFichierDtoModif processDemandeModif(LigneFichierDto ligneFichierDto) throws CBSException, ZoneException, IOException {
        DemandeModif demandeModif = (DemandeModif) demande;
        LigneFichierDtoModif ligneFichierDtoModif = (LigneFichierDtoModif) ligneFichierDto;
        //sauvegarde la notice modifiée
        this.proxyRetry.saveExemplaire(demandeModif, ligneFichierDtoModif);
        ligneFichierDtoModif.setRetourSudoc(Constant.EXEMPLAIRE_MODIFIE);
        return ligneFichierDtoModif;
    }

    /**
     * Méthode permettant de lancer l'exemplarisation sur une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer le traitement d'exemplarisation
     * @return la DTO de la ligne fichier modifiée en fonction du résultat du traitement
     * @throws CBSException  : erreur CBS
     * @throws ZoneException : erreur de construction de la notice
     * @throws IOException : erreur de communication avec le CBS
     */
    private LigneFichierDtoExemp processDemandeExemp(LigneFichierDto ligneFichierDto) throws CBSException, ZoneException, IOException {
        DemandeExemp demandeExemp = (DemandeExemp) this.demande;
        LigneFichierDtoExemp ligneFichierDtoExemp = (LigneFichierDtoExemp) ligneFichierDto;
        this.proxyRetry.newExemplaire(demandeExemp, ligneFichierDtoExemp);
        return ligneFichierDtoExemp;
    }


    /**
     * Méthode permettant de lancer le test de recouvrement pour une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer la requête
     * @return la DTO ligneFichier mise à jour en fonction du résultat de la requête che
     * @throws CBSException          : erreur CBS
     * @throws QueryToSudocException : erreur dans le type d'index de recherche
     * @throws IOException         : erreur de communication avec le CBS
     */
    private LigneFichierDtoRecouv processDemandeRecouv(LigneFichierDto ligneFichierDto) throws CBSException, QueryToSudocException, IOException {
        DemandeRecouv demandeRecouv = (DemandeRecouv) this.demande;
        LigneFichierDtoRecouv ligneFichierDtoRecouv = (LigneFichierDtoRecouv) ligneFichierDto;
        this.proxyRetry.recouvExemplaire(demandeRecouv, ligneFichierDtoRecouv);
        return ligneFichierDtoRecouv;
    }
}
