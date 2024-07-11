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
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.service.FileSystemStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
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

    private final FileSystemStorageService storageService;

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

    public JobConfiguration(StrategyFactory strategyFactory, ProxyRetry proxyRetry, FileSystemStorageService storageService) {
        this.strategyFactory = strategyFactory;
        this.proxyRetry = proxyRetry;
        this.storageService = storageService;
    }

    @Bean
    public ExecutionContextSerializer configureSerializer() {
        return new Jackson2ExecutionContextStringSerializer(LigneFichierDtoModif.class.getName(), LigneFichierDtoRecouv.class.getName(), LigneFichierDtoExemp.class.getName());
    }

    // ----- CHUNK ------
    @Bean
    public LignesFichierReader reader() {
        return new LignesFichierReader(proxyRetry);
    }
    @Bean
    public LignesFichierProcessor processor() {
        return new LignesFichierProcessor(strategyFactory, proxyRetry);
    }
    @Bean
    public LignesFichierWriter writer() {
        return new LignesFichierWriter(strategyFactory);
    }

    // ------------- TASKLETS -----------------------
    @Bean
    public Tasklet getNextDemandeModifTasklet() { return new GetNextDemandeModifTasklet(strategyFactory, minHour, maxHour); }
    @Bean
    public Tasklet getNextDemandeExempTasklet() { return new GetNextDemandeExempTasklet(strategyFactory, minHour, maxHour); }
    @Bean
    public Tasklet getNextDemandeRecouvTasklet() { return new GetNextDemandeRecouvTasklet(strategyFactory, minHour, maxHour); }
    @Bean
    public Tasklet lireLigneFichierTasklet() { return new LireLigneFichierTasklet(strategyFactory, mailAdmin); }
    @Bean
    public Tasklet authentifierSurSudocTasklet()
    {
        return new AuthentifierSurSudocTasklet(strategyFactory, mailAdmin, proxyRetry);
    }
    @Bean
    public Tasklet genererFichierTasklet() { return new GenererFichierTasklet(strategyFactory, uploadPath, mailAdmin, nbPpnInFileResult); }
    @Bean
    public Tasklet verifierParamsTasklet() { return new VerifierParamsTasklet(); }

    //statistiques application
    @Bean
    public Tasklet exportStatistiquesTasklet() { return new ExportStatistiquesTasklet(); }


    //Archivage automatique des demandes
    @Bean
    Tasklet archiveDemandesExempTakslet(){
        return new ArchiveDemandesExempTakslet(strategyFactory);
    }
    @Bean
    Tasklet archiveDemandesModifTasklet(){
        return new ArchiveDemandesModifTasklet(strategyFactory);
    }
    @Bean
    Tasklet archiveDemandesRecouvTasklet(){
        return new ArchiveDemandesRecouvTasklet(strategyFactory);
    }

    //Passage en statut supprimé automatique des demandes
    @Bean
    Tasklet deleteStatusDemandesExempTasklet(){
        return new DeleteStatusDemandesExempTasklet(strategyFactory);
    }
    @Bean
    Tasklet deleteStatusDemandesModifTasklet(){
        return new DeleteStatusDemandesModifTasklet(strategyFactory);
    }
    @Bean
    Tasklet deleteStatusDemandesRecouvTasklet(){
        return new DeleteStatusDemandesRecouvTasklet(strategyFactory);
    }

    //Suppression définitive des demandes
    @Bean
    Tasklet deleteDemandesExempTasklet(){
        return new DeleteDemandesExempTasklet(strategyFactory, storageService, uploadPath);
    }
    @Bean
    Tasklet deleteDemandesModifTasklet(){
        return new DeleteDemandesModifTasklet(strategyFactory, storageService, uploadPath);
    }
    @Bean
    Tasklet deleteDemandesRecouvTasklet(){
        return new DeleteDemandesRecouvTasklet(strategyFactory, storageService, uploadPath);
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
    public Job jobArchivageDemandes(JobRepository jobRepository, @Qualifier("stepArchivageAutomatiqueDemandesExemp") Step step1, @Qualifier("stepArchivageAutomatiqueDemandesModif") Step step2, @Qualifier("stepArchivageAutomatiqueDemandesRecouv") Step step3) {
        return new JobBuilder("archiverDemandesPlusDeTroisMois", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).end()
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.AUCUNE_DEMANDE).end()
                .from(step2).on(Constant.COMPLETED).end()
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.AUCUNE_DEMANDE).end()
                .from(step3).on(Constant.COMPLETED).end()
                .build().build();
    }

    //Job de placement en statut supprimé de toutes les demandes en statut archivé dont ce statut à plus de trois mois
    @Bean
    public Job jobSuppressionMaisConservationEnBaseDemandes(JobRepository jobRepository, @Qualifier("stepChangementStatutSupprimeDemandesExemp") Step step1, @Qualifier("stepChangementStatutSupprimeDemandesModif") Step step2, @Qualifier("stepChangementStatutSupprimeDemandesRecouv") Step step3) {
        return new JobBuilder("statutSupprimeDemandesPlusDeTroisMois", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).end()
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.AUCUNE_DEMANDE).end()
                .from(step2).on(Constant.COMPLETED).end()
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.AUCUNE_DEMANDE).end()
                .from(step3).on(Constant.COMPLETED).end()
                .build().build();
    }

    //Job de suppression définitive en base de donnée de toutes les demandes en statut supprimé, dont ce statut à plus de trois mois
    @Bean
    public Job jobSuppressionDefinitiveDemandes(JobRepository jobRepository, @Qualifier("stepSuppresionDemandesExemp") Step step1, @Qualifier("stepSuppresionDemandesModif") Step step2, @Qualifier("stepSuppresionDemandesRecouv") Step step3) {
        return new JobBuilder("suppressionDemandesPlusDeTroisMois", jobRepository).incrementer(incrementer())
                .start(step1).on(Constant.FAILED).end()
                .from(step1).on(Constant.AUCUNE_DEMANDE).end()
                .from(step1).on(Constant.COMPLETED).end()
                .from(step2).on(Constant.FAILED).end()
                .from(step2).on(Constant.AUCUNE_DEMANDE).end()
                .from(step2).on(Constant.COMPLETED).end()
                .from(step3).on(Constant.FAILED).end()
                .from(step3).on(Constant.AUCUNE_DEMANDE).end()
                .from(step3).on(Constant.COMPLETED).end()
                .build().build();
    }


    // ------------------ INCREMENTER ------------------
    protected JobParametersIncrementer incrementer() {
        return new TimeIncrementer();
    }

}
