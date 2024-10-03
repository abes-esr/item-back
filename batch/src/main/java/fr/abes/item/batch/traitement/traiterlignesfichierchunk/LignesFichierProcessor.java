package fr.abes.item.batch.traitement.traiterlignesfichierchunk;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.batch.traitement.ProxyRetry;
import fr.abes.item.batch.traitement.model.*;
import fr.abes.item.core.components.FichierSauvegardeSuppCsv;
import fr.abes.item.core.components.FichierSauvegardeSuppTxt;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.exception.StorageException;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.service.impl.DemandeSuppService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class LignesFichierProcessor implements ItemProcessor<LigneFichierDto, LigneFichierDto>, StepExecutionListener {
    private final StrategyFactory strategyFactory;
    private final ProxyRetry proxyRetry;
    private final ReferenceService referenceService;
    private FichierSauvegardeSuppTxt fichierSauvegardeSuppTxt;
    private FichierSauvegardeSuppCsv fichierSauvegardeSuppcsv;

    private Demande demande;

    public LignesFichierProcessor(StrategyFactory strategyFactory, ProxyRetry proxyRetry,ReferenceService referenceService) {
        this.strategyFactory = strategyFactory;
        this.proxyRetry = proxyRetry;
        this.referenceService = referenceService;
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        TYPE_DEMANDE typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        IDemandeService demandeService = strategyFactory.getStrategy(IDemandeService.class, typeDemande);
        Integer demandeId = (Integer) executionContext.get("demandeId");
        this.demande = demandeService.findById(demandeId);
        this.fichierSauvegardeSuppTxt = new FichierSauvegardeSuppTxt();
        this.fichierSauvegardeSuppTxt.setPath(Path.of(String.valueOf(executionContext.get("fichierTxtPath"))));
        this.fichierSauvegardeSuppTxt.setFilename(String.valueOf(executionContext.get("fichierTxtName")));

        this.fichierSauvegardeSuppcsv = new FichierSauvegardeSuppCsv(this.referenceService);
        this.fichierSauvegardeSuppcsv.setPath(Path.of(String.valueOf(executionContext.get("fichierCsvPath"))));
        this.fichierSauvegardeSuppcsv.setFilename(String.valueOf(executionContext.get("fichierCsvName")));

        log.info(Constant.POUR_LA_DEMANDE + this.demande.getNumDemande());
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return null;
    }

    @Override
    public LigneFichierDto process(@NonNull LigneFichierDto ligneFichierDto) {
        try {
            return switch (ligneFichierDto.getTypeDemande()) {
                case MODIF -> processDemandeModif(ligneFichierDto);
                case EXEMP -> processDemandeExemp(ligneFichierDto);
                case SUPP -> processDemandeSupp(ligneFichierDto);
                default -> processDemandeRecouv(ligneFichierDto);
            };
        } catch (CBSException | ZoneException | QueryToSudocException | IOException e) {
            log.error(Constant.ERROR_FROM_SUDOC_REQUEST_OR_METHOD_SAVEXEMPLAIRE + e);
            ligneFichierDto.setRetourSudoc(e.getMessage());
        } catch (JDBCConnectionException | ConstraintViolationException j) {
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException d) {
            if (d.getRootCause() instanceof SQLException sqlEx) {
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }
        } catch (StorageException ex) {
            log.error(ex.getMessage());
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
     * @throws IOException   : erreur de communication avec le CBS
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
     * @throws IOException   : erreur de communication avec le CBS
     */
    private LigneFichierDtoExemp processDemandeExemp(LigneFichierDto ligneFichierDto) throws CBSException, ZoneException, IOException {
        DemandeExemp demandeExemp = (DemandeExemp) this.demande;
        LigneFichierDtoExemp ligneFichierDtoExemp = (LigneFichierDtoExemp) ligneFichierDto;
        this.proxyRetry.newExemplaire(demandeExemp, ligneFichierDtoExemp);
        return ligneFichierDtoExemp;
    }


    /**
     * Méthode permettant de lancer la suppression sur une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur lequel lancer le traitement de suppression
     * @return la DTO de la ligne fichier modifiée en fonction du résultat du traitement
     * @throws CBSException : erreur CBS
     * @throws IOException  : erreur de communication avec le CBS
     */
    private LigneFichierDtoSupp processDemandeSupp(LigneFichierDto ligneFichierDto) throws CBSException, IOException, ZoneException, QueryToSudocException, StorageException {
        DemandeSupp demandeSupp = (DemandeSupp) this.demande;
        LigneFichierDtoSupp ligneFichierDtoSupp = (LigneFichierDtoSupp) ligneFichierDto;
        //récupération des exemplaires existants pour cette ligne
        List<Exemplaire> exemplairesExistants = ((DemandeSuppService) strategyFactory.getStrategy(IDemandeService.class, TYPE_DEMANDE.SUPP))
                .getExemplairesExistants(ligneFichierDtoSupp.getPpn());
        Optional<Exemplaire> exemplaireASupprimerOpt = exemplairesExistants.stream().filter(exemplaire -> exemplaire.findZone("A99", 0).getValeur().equals(ligneFichierDtoSupp.getEpn())).findFirst();
        if (exemplaireASupprimerOpt.isPresent()){
            this.fichierSauvegardeSuppTxt.writePpnInFile(ligneFichierDtoSupp.getPpn(), exemplaireASupprimerOpt.get());
            this.fichierSauvegardeSuppcsv.writePpnInFile(ligneFichierDtoSupp.getPpn(), exemplaireASupprimerOpt.get());
        }
        //supprimer l'exemplaire
        this.proxyRetry.deleteExemplaire(demandeSupp, ligneFichierDtoSupp);
        ligneFichierDtoSupp.setRetourSudoc(Constant.EXEMPLAIRE_SUPPRIME);
        return ligneFichierDtoSupp;
    }

    private String getL035fromDonneesLocales(String donneeLocale) throws ZoneException {
        DonneeLocale donneeLocale1 = new DonneeLocale(donneeLocale);
        List<Zone> listeL035 = donneeLocale1.findZones("L035");
        if (!listeL035.isEmpty()) {
            return listeL035.get(listeL035.size() - 1).findSubLabel("$a");
        }
        return null;
    }

    /**
     * Méthode permettant de lancer le test de recouvrement pour une ligne du fichier
     *
     * @param ligneFichierDto ligne du fichier sur laquelle lancer la requête
     * @return la DTO ligneFichier mise à jour en fonction du résultat de la requête che
     * @throws CBSException          : erreur CBS
     * @throws QueryToSudocException : erreur dans le type d'index de recherche
     * @throws IOException           : erreur de communication avec le CBS
     */
    private LigneFichierDtoRecouv processDemandeRecouv(LigneFichierDto ligneFichierDto) throws CBSException, QueryToSudocException, IOException {
        DemandeRecouv demandeRecouv = (DemandeRecouv) this.demande;
        LigneFichierDtoRecouv ligneFichierDtoRecouv = (LigneFichierDtoRecouv) ligneFichierDto;
        this.proxyRetry.recouvExemplaire(demandeRecouv, ligneFichierDtoRecouv);
        return ligneFichierDtoRecouv;
    }
}
