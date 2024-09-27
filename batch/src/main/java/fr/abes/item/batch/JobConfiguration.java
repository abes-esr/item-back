package fr.abes.item.batch;

import fr.abes.item.batch.traitement.*;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.batch.traitement.model.LigneFichierDtoModif;
import fr.abes.item.batch.traitement.model.LigneFichierDtoRecouv;
import fr.abes.item.batch.traitement.traiterlignesfichierchunk.LignesFichierProcessor;
import fr.abes.item.batch.traitement.traiterlignesfichierchunk.LignesFichierReader;
import fr.abes.item.batch.traitement.traiterlignesfichierchunk.LignesFichierWriter;
import fr.abes.item.batch.webstats.ExportStatistiquesTasklet;
import fr.abes.item.batch.webstats.VerifierParamsTasklet;
import fr.abes.item.core.components.FichierSauvegardeSupp;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;


@Slf4j
@Configuration
@EnableRetry
@ComponentScans(value = {
        @ComponentScan(basePackages = {"fr.abes.item.core.repository.item"}),
        @ComponentScan(basePackages = {"fr.abes.item.core.service"}),
        @ComponentScan(basePackages = {"fr.abes.item.core.configuration"}),
        @ComponentScan(basePackages = {"fr.abes.item.core.components"})
})
@EntityScan("fr.abes.item.core.entities.item")
public class JobConfiguration {
    private final StrategyFactory strategyFactory;
    private final ProxyRetry proxyRetry;
    private final FichierSauvegardeSupp fichierSauvegardeSupp;

    @Value("${batch.min.hour}")
    int minHour;

    @Value("${batch.max.hour}")
    int maxHour;

    @Value("${mail.admin}")
    private String mailAdmin;

    @Value("${files.upload.path}")
    private String uploadPath;
    @Value("${batch.nbPpnInFileResult}")
    private Integer nbPpnInFileResult;


    public JobConfiguration(StrategyFactory strategyFactory, ProxyRetry proxyRetry, FichierSauvegardeSupp fichierSauvegardeSupp) {
        this.strategyFactory = strategyFactory;
        this.proxyRetry = proxyRetry;
        this.fichierSauvegardeSupp = fichierSauvegardeSupp;
    }

    @Bean
    public ExecutionContextSerializer configureSerializer() {
        return new Jackson2ExecutionContextStringSerializer(LigneFichierDtoModif.class.getName(), LigneFichierDtoRecouv.class.getName(), LigneFichierDtoExemp.class.getName());
    }

    // ----- CHUNK ------
    @Bean
    public LignesFichierReader reader() {
        return new LignesFichierReader();
    }

    @Bean
    @StepScope
    public LignesFichierProcessor processor() {
        return new LignesFichierProcessor(strategyFactory, proxyRetry, fichierSauvegardeSupp);
    }
    @Bean
    public LignesFichierWriter writer() {
        return new LignesFichierWriter(strategyFactory);
    }

    // ------------- TASKLETS -----------------------
    @Bean
    public Tasklet getNextDemandeModifTasklet() { return new GetNextDemandeTasklet(strategyFactory, minHour, maxHour, TYPE_DEMANDE.MODIF); }
    @Bean
    public Tasklet getNextDemandeExempTasklet() { return new GetNextDemandeTasklet(strategyFactory, minHour, maxHour, TYPE_DEMANDE.EXEMP); }
    @Bean
    public Tasklet getNextDemandeRecouvTasklet() { return new GetNextDemandeTasklet(strategyFactory, minHour, maxHour, TYPE_DEMANDE.RECOUV); }
    @Bean
    public Tasklet getNextDemandeSuppTasklet() { return new GetNextDemandeTasklet(strategyFactory, minHour, maxHour, TYPE_DEMANDE.SUPP); }
    @Bean
    public Tasklet lireLigneFichierTasklet() { return new LireLigneFichierTasklet(strategyFactory, mailAdmin); }
    @Bean
    public Tasklet authentifierSurSudocTasklet()
    {
        return new AuthentifierSurSudocTasklet(strategyFactory, mailAdmin, proxyRetry);
    }
    @Bean
    public Tasklet genererFichierTasklet() { return new GenererFichierTasklet(strategyFactory, uploadPath, mailAdmin, nbPpnInFileResult, fichierSauvegardeSupp); }
    @Bean
    public Tasklet verifierParamsTasklet() { return new VerifierParamsTasklet(); }

    //statistiques application
    @Bean
    public Tasklet exportStatistiquesTasklet() { return new ExportStatistiquesTasklet(); }


    //Archivage automatique des demandes
    @Bean
    Tasklet archiveDemandesExempTakslet(){ return new ArchiveDemandesTakslet(strategyFactory, TYPE_DEMANDE.EXEMP); }
    @Bean
    Tasklet archiveDemandesModifTasklet(){ return new ArchiveDemandesTakslet(strategyFactory, TYPE_DEMANDE.MODIF); }
    @Bean
    Tasklet archiveDemandesRecouvTasklet(){ return new ArchiveDemandesTakslet(strategyFactory, TYPE_DEMANDE.RECOUV); }
    @Bean
    Tasklet archiveDemandesSuppTasklet(){ return new ArchiveDemandesTakslet(strategyFactory, TYPE_DEMANDE.SUPP); }

    //Passage en statut supprimé automatique des demandes
    @Bean
    Tasklet deleteStatusDemandesExempTasklet(){ return new DeleteStatusDemandesTasklet(strategyFactory, TYPE_DEMANDE.EXEMP); }
    @Bean
    Tasklet deleteStatusDemandesModifTasklet(){ return new DeleteStatusDemandesTasklet(strategyFactory, TYPE_DEMANDE.MODIF); }
    @Bean
    Tasklet deleteStatusDemandesRecouvTasklet(){ return new DeleteStatusDemandesTasklet(strategyFactory, TYPE_DEMANDE.RECOUV); }
    @Bean
    Tasklet deleteStatusDemandesSuppTasklet() { return new DeleteStatusDemandesTasklet(strategyFactory, TYPE_DEMANDE.SUPP); }

    //Suppression définitive des demandes
    @Bean
    Tasklet deleteDemandesExempTasklet(){
        return new DeleteDemandesTasklet(strategyFactory, TYPE_DEMANDE.EXEMP);
    }
    @Bean
    Tasklet deleteDemandesModifTasklet(){
        return new DeleteDemandesTasklet(strategyFactory, TYPE_DEMANDE.MODIF);
    }
    @Bean
    Tasklet deleteDemandesRecouvTasklet(){
        return new DeleteDemandesTasklet(strategyFactory, TYPE_DEMANDE.RECOUV);
    }
    @Bean
    Tasklet deleteDemandesSuppTasklet() {
        return new DeleteDemandesTasklet(strategyFactory, TYPE_DEMANDE.SUPP);
    }

    // ---------- STEP --------------------------------------------

    @Bean
    public Step stepRecupererNextDemandeModif(JobRepository jobRepository, @Qualifier("getNextDemandeModifTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeModif", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeExemp(JobRepository jobRepository, @Qualifier("getNextDemandeExempTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeRecouv(JobRepository jobRepository, @Qualifier("getNextDemandeRecouvTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeSupp(JobRepository jobRepository, @Qualifier("getNextDemandeSuppTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeSupp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    // Steps pour lancement d'un traitement de modification de masse
    @Bean
    public Step stepLireLigneFichier(JobRepository jobRepository, @Qualifier("lireLigneFichierTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepLireLigneFichier", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepAuthentifierSurSudoc(JobRepository jobRepository, @Qualifier("authentifierSurSudocTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepAuthentifierSurSudoc", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    protected Step stepTraiterLigneFichier(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemReader<LigneFichierDto> reader, ItemProcessor<LigneFichierDto, LigneFichierDto> processor, ItemWriter<LigneFichierDto> writer) {
        return new StepBuilder("stepTraiterLigneFichier", jobRepository).<LigneFichierDto, LigneFichierDto> chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    //Step s'éxecutant en cas d'erreur survenant pendant le traitement des lignes du fichier
    @Bean
    @Qualifier("stepTraitementErreurStep4")
    public Step stepTraitementErreurStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepTraitementErreurStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Logique de traitement d'erreur ici
                    // Par exemple, vous pouvez logger l'erreur, envoyer une notification, etc.
                    log.error("Erreur lors du traitement de la ligne de fichier");
                    // Vous pouvez aussi accéder aux informations de l'erreur via le JobExecution
                    JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
                    Throwable exception = jobExecution.getAllFailureExceptions().get(0);
                    log.error("Exception: ", exception);
                    //Stocker le fichier sur le serveur

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    @Bean
    public Step stepGenererFichier(JobRepository jobRepository, @Qualifier("genererFichierTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepGenererFichier", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // Steps pour exports statistiques
    @Bean
    public Step stepVerifierParams(JobRepository jobRepository, @Qualifier("verifierParamsTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepVerifierParams", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step stepExportStatistiques(JobRepository jobRepository, @Qualifier("exportStatistiquesTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepExportStatistiques", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }


    //Steps d'archivage automatique des demandes
    @Bean
    public Step stepArchivageAutomatiqueDemandesExemp(JobRepository jobRepository, @Qualifier("archiveDemandesExempTakslet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesModif(JobRepository jobRepository, @Qualifier("archiveDemandesModifTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesModif", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesRecouv(JobRepository jobRepository, @Qualifier("archiveDemandesRecouvTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesSupp(JobRepository jobRepository, @Qualifier("archiveDemandesSuppTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesSupp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    //Steps de placement en statut supprimé automatique des demandes
    @Bean
    public Step stepChangementStatutSupprimeDemandesExemp(JobRepository jobRepository, @Qualifier("deleteStatusDemandesExempTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepChangementStatutSupprimeDemandesModif(JobRepository jobRepository, @Qualifier("deleteStatusDemandesModifTasklet") Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesModif", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepChangementStatutSupprimeDemandesRecouv(JobRepository jobRepository, @Qualifier("deleteStatusDemandesRecouvTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step stepChangementStatutSupprimeDemandesSupp(JobRepository jobRepository, @Qualifier("deleteStatusDemandesSuppTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesSupp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    //Steps de destruction en base de donnée des demandes
    @Bean
    public Step stepSuppresionDemandesExemp(JobRepository jobRepository, @Qualifier("deleteDemandesExempTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppresionDemandesExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepSuppresionDemandesModif(JobRepository jobRepository, @Qualifier("deleteDemandesModifTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppresionDemandesModif", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepSuppresionDemandesRecouv(JobRepository jobRepository, @Qualifier("deleteDemandesRecouvTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppresionDemandesRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step stepSuppressionDemandesSupp(JobRepository jobRepository, @Qualifier("deleteDemandesSuppTasklet")Tasklet tasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppressionDemandesSupp", jobRepository).allowStartIfComplete(true)
                .tasklet(tasklet, transactionManager)
                .build();
    }


    // ---------- JOB ---------------------------------------------

    // Job de lancement d'un traitement de modification
    @Bean
    public Job jobTraiterLigneFichier(JobRepository jobRepository, @Qualifier("stepRecupererNextDemandeModif") Step step1, @Qualifier("stepLireLigneFichier") Step step2, @Qualifier("stepAuthentifierSurSudoc") Step step3, @Qualifier("stepTraiterLigneFichier") Step step4, @Qualifier("stepGenererFichier") Step step5) {
        return new JobBuilder("traiterLigneFichierModif", jobRepository ).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).to(step2)
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.COMPLETED).to(step3)
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.COMPLETED).to(step4)
                .from(step4).on(Constant.FAILED).end()
                .from(step4).on(Constant.COMPLETED).to(step5)
                .build().build();
    }

    //job de lancement d'un traitement d'exemplarisation
    @Bean
    public Job jobTraiterLigneFichierExemp(JobRepository jobRepository, @Qualifier("stepRecupererNextDemandeExemp") Step step1, @Qualifier("stepLireLigneFichier") Step step2, @Qualifier("stepAuthentifierSurSudoc") Step step3, @Qualifier("stepTraiterLigneFichier") Step step4, @Qualifier("stepGenererFichier") Step step5) {
        return new JobBuilder("traiterLigneFichierExemp", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).to(step2)
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.COMPLETED).to(step3)
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.COMPLETED).to(step4)
                .from(step4).on(Constant.FAILED).end()
                .from(step4).on(Constant.COMPLETED).to(step5)
                .build().build();
    }

    //job de lancement d'un test de recouvrement
    @Bean
    public Job jobTraiterLigneFichierRecouv(JobRepository jobRepository, @Qualifier("stepRecupererNextDemandeRecouv") Step step1, @Qualifier("stepLireLigneFichier") Step step2, @Qualifier("stepAuthentifierSurSudoc") Step step3, @Qualifier("stepTraiterLigneFichier") Step step4, @Qualifier("stepGenererFichier") Step step5) {
        return new JobBuilder("traiterLigneFichierRecouv", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).to(step2)
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.COMPLETED).to(step3)
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.COMPLETED).to(step4)
                .from(step4).on(Constant.FAILED).end()
                .from(step4).on(Constant.COMPLETED).to(step5)
                .build().build();
    }

    //job de lancement d'une demande de suppression
    @Bean
    public Job jobTraiterLigneFichierSupp(JobRepository jobRepository, @Qualifier("stepRecupererNextDemandeSupp") Step step1, @Qualifier("stepLireLigneFichier") Step step2, @Qualifier("stepAuthentifierSurSudoc") Step step3, @Qualifier("stepTraiterLigneFichier") Step step4, @Qualifier("stepTraitementErreurStep4") Step stepTraitementErreurStep4, @Qualifier("stepGenererFichier") Step step5) {
        return new JobBuilder("traiterLigneFichierSupp", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).to(step2)
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.COMPLETED).to(step3)
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.COMPLETED).to(step4)
                .from(step4).on(Constant.FAILED).to(stepTraitementErreurStep4)
                .from(stepTraitementErreurStep4).on(Constant.COMPLETED).end()
                .from(step4).on(Constant.COMPLETED).to(step5)
                .build().build();
    }

    // Job d'export des statistiques mensuelles
    @Bean
    public Job jobExportStatistiques(JobRepository jobRepository, @Qualifier("stepVerifierParams") Step step1, @Qualifier("stepExportStatistiques") Step step2) {
        return new JobBuilder("exportStatistiques", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.COMPLETED).to(step2)
                .build().build();
    }

    //Job d'archivage automatique de toutes les demandes en statut terminé dont la dernière modification à plus de trois mois
    @Bean
    public Job jobArchivageDemandes(JobRepository jobRepository, @Qualifier("stepArchivageAutomatiqueDemandesExemp") Step step1, @Qualifier("stepArchivageAutomatiqueDemandesModif") Step step2, @Qualifier("stepArchivageAutomatiqueDemandesRecouv") Step step3, @Qualifier("stepArchivageAutomatiqueDemandesSupp") Step step4) {
        return new JobBuilder("archivageDemandes", jobRepository).incrementer(incrementer())
                .start(step1).next(step2).next(step3).next(step4)
                .build();
    }

    //Job de placement en statut supprimé de toutes les demandes en statut archivé dont ce statut à plus de trois mois
    @Bean
    public Job jobSuppressionMaisConservationEnBaseDemandes(JobRepository jobRepository, @Qualifier("stepChangementStatutSupprimeDemandesExemp") Step step1, @Qualifier("stepChangementStatutSupprimeDemandesModif") Step step2, @Qualifier("stepChangementStatutSupprimeDemandesRecouv") Step step3, @Qualifier("stepChangementStatutSupprimeDemandesSupp") Step step4) {
        return new JobBuilder("suppressionDemandesPlusDeTroisMois", jobRepository).incrementer(incrementer())
                .start(step1).next(step2).next(step3).next(step4)
                .build();
    }

    //Job de suppression définitive en base de donnée de toutes les demandes en statut supprimé, dont ce statut à plus de trois mois
    @Bean
    public Job jobSuppressionDefinitiveDemandes(JobRepository jobRepository, @Qualifier("stepSuppresionDemandesExemp") Step step1, @Qualifier("stepSuppresionDemandesModif") Step step2, @Qualifier("stepSuppresionDemandesRecouv") Step step3, @Qualifier("stepSuppressionDemandesSupp") Step step4) {
        return new JobBuilder("suppressionDefinitiveDemandes", jobRepository).incrementer(incrementer())
                .start(step1).next(step2).next(step3).next(step4)
                .build();
    }


    // ------------------ INCREMENTER ------------------
    protected JobParametersIncrementer incrementer() {
        return new TimeIncrementer();
    }

}
