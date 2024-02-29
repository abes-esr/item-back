package fr.abes.item.traitement;

import fr.abes.item.LogTime;
import fr.abes.item.components.FichierResultat;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.FileTypeException;
import fr.abes.item.exception.QueryToSudocException;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.IDemandeService;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.FichierFactory;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.service.impl.DemandeExempService;
import fr.abes.item.service.impl.DemandeRecouvService;
import fr.abes.item.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.traitement.model.LigneFichierDtoModif;
import fr.abes.item.traitement.model.LigneFichierDtoRecouv;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
public class GenererFichierTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    private StrategyFactory factory;

    @Autowired
    private DemandeExempService demandeExempService;
    @Autowired
    private DemandeRecouvService demandeRecouvService;
    private IDemandeService demandeService;

    ILigneFichierService ligneFichierService;

    IMailer mailer;

    @Value("${files.upload.path}")
    private String uploadPath;

    @Value("${mail.admin}")
    private String mailAdmin;

    @Value("${batch.nbPpnInFileResult}")
    private Integer nbPpnInFileResult;

    private LocalDateTime dateDebut;
    private String email;
    private Demande demande;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        TYPE_DEMANDE typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        Integer demandeId = (Integer) executionContext.get("demandeId");
        this.demandeService = factory.getStrategy(IDemandeService.class, typeDemande);
        this.demande = demandeService.findById(demandeId);
        this.email = this.demande.getUtilisateur().getEmail() + ";" + mailAdmin;
        this.mailer = factory.getStrategy(IMailer.class, demande.getTypeDemande());
        this.ligneFichierService = factory.getStrategy(ILigneFichierService.class, demande.getTypeDemande());
        this.dateDebut = stepExecution.getJobExecution().getCreateTime();
    }

    /**
     * Méthode d'exécution du step de génération du fichier de résultat
     *
     * @param contribution : contexte des steps exécutés pour récupérer la demande
     * @param chunkContext : contexte du chunk traité
     * @return : statut d'exécution du step (FINISHED, FAILED)
     * @throws Exception : erreur lambda
     */
    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        log.warn(Constant.ENTER_EXECUTE_FROM_GENEREFICHIER);

        try {
            String nomFichier = this.genererFichier(); //NOM FICHIER DEMANDE GENERE
            mailer.mailFinTraitement(
                    this.email, //EMAIL UTULISATEUR
                    demande,
                    Paths.get(uploadPath + demande.getId() //uploadPath = applis kopya retourne FILE de la demandeModif
                            + "/" + nomFichier).toFile(), dateDebut, LocalDateTime.now());
        } catch (Exception e) {
            log.error(Constant.ERROR_WHILE_GENERATING_THE_FILE_OR_WHILE_SENDING_MAIL + e);
            mailer.mailEchecTraitement(
                    this.email,
                    this.demande,
                    this.dateDebut
            );
            mailer.mailAlertAdmin(this.mailAdmin, demande);
            demandeService.changeState(demande, Constant.ETATDEM_ERREUR);
            contribution.setExitStatus(ExitStatus.FAILED);
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * Méthode de génération du fichier de résultat
     *
     * @return nom du fichier
     * @throws IOException : erreur accès fichier
     * @throws FileTypeException : erreur de type de fichier
     */
    private String genererFichier() throws IOException, FileTypeException, QueryToSudocException {
        FichierResultat fichierResultat;
        IDemandeService demandeService = factory.getStrategy(IDemandeService.class, demande.getTypeDemande());

        fichierResultat = (FichierResultat) FichierFactory.getFichier(Constant.ETATDEM_ENCOURS, demande.getTypeDemande());
        fichierResultat.generateFileName(demande.getId());
        fichierResultat.setPath(Paths.get(uploadPath + demande.getId()));

        try (FileWriter fw = new FileWriter(fichierResultat.getPath().resolve(fichierResultat.getFilename()).toString(), false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // en tête du fichier
            out.println(demandeService.getInfoHeaderFichierResultat(demande, dateDebut));
            for (LigneFichier ligne : this.ligneFichierService.getLigneFichierTraiteeByDemande(demande)) {
                switch (demande.getTypeDemande()) {
                    case EXEMP:
                        DemandeExemp demandeExemp = (DemandeExemp) demande;
                        LigneFichierDtoExemp ligneFichierDtoExemp = new LigneFichierDtoExemp((LigneFichierExemp) ligne);
                        log.warn(ligneFichierDtoExemp.getIndexRecherche());
                        ligneFichierDtoExemp.setRequete(demandeExempService.getQueryToSudoc(demandeExemp.getIndexRecherche().getCode(), demandeExemp.getTypeExemp().getLibelle(), ligneFichierDtoExemp.getIndexRecherche().split(";")));
                        out.println(ligneFichierDtoExemp.getValeurToWriteInFichierResultat(demande, nbPpnInFileResult));
                        break;
                    case MODIF:
                        LigneFichierDtoModif ligneFichierDtoModif = new LigneFichierDtoModif((LigneFichierModif) ligne);
                        out.println(ligneFichierDtoModif.getValeurToWriteInFichierResultat(demande, nbPpnInFileResult));
                        break;
                    default:
                        DemandeRecouv demandeRecouv = (DemandeRecouv)demande;
                        LigneFichierDtoRecouv ligneFichierDtoRecouv = new LigneFichierDtoRecouv((LigneFichierRecouv) ligne);
                        ligneFichierDtoRecouv.setRequete(demandeRecouvService.getQueryToSudoc(demandeRecouv.getIndexRecherche().getCode(), ligneFichierDtoRecouv.getIndexRecherche().split(";")));
                        out.println(ligneFichierDtoRecouv.getValeurToWriteInFichierResultat(demande, nbPpnInFileResult));
                        break;
                }
                //ligne correspondant au résultat du traitement de chaque ligne du fichier d'origine
            }
            return fichierResultat.getFilename();
        } catch (IOException | QueryToSudocException | DataAccessException ex) {
            log.error(Constant.ERROR_WHILE_CREATING_RESULT_FILE_IN_EXECUTE + ex);
            throw ex;
        }
    }


    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        LogTime.logFinTraitement(stepExecution);
        return null;
    }
}
