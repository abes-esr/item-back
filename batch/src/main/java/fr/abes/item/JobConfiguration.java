package fr.abes.item;

import fr.abes.item.configuration.BaseXMLConfiguration;
import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.constant.Constant;
import fr.abes.item.traitement.*;
import fr.abes.item.traitement.model.LigneFichierDto;
import fr.abes.item.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.traitement.model.LigneFichierDtoModif;
import fr.abes.item.traitement.model.LigneFichierDtoRecouv;
import fr.abes.item.traitement.traiterlignesfichierchunk.LignesFichierProcessor;
import fr.abes.item.traitement.traiterlignesfichierchunk.LignesFichierReader;
import fr.abes.item.traitement.traiterlignesfichierchunk.LignesFichierWriter;
import fr.abes.item.webstats.ExportStatistiquesTasklet;
import fr.abes.item.webstats.VerifierParamsTasklet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;


@Slf4j
@Configuration
@EnableRetry
@EnableBatchProcessing
@ComponentScans(value = {
        @ComponentScan(basePackages = {"fr.abes.item.repository"},
                excludeFilters = {
                        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = BaseXMLConfiguration.class)
                },
                includeFilters = {
                        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ItemConfiguration.class)}
        ),
        @ComponentScan(basePackages = {"fr.abes.item.configuration"},
                excludeFilters = {
                        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = BaseXMLConfiguration.class)
                })
})
@EntityScan("fr.abes.item.entities.item")
public class JobConfiguration {
    @Value("${batch.min.hour}")
    int minHour;

    @Value("${batch.max.hour}")
    int maxHour;

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
    public LignesFichierProcessor processor() {
        return new LignesFichierProcessor();
    }
    @Bean
    public LignesFichierWriter writer() {
        return new LignesFichierWriter();
    }

    // ------------- TASKLETS -----------------------
    @Bean
    public GetNextDemandeModifTasklet getNextDemandeModifTasklet() { return new GetNextDemandeModifTasklet(minHour, maxHour); }
    @Bean
    public GetNextDemandeExempTasklet getNextDemandeExempTasklet() { return new GetNextDemandeExempTasklet(minHour, maxHour); }
    @Bean
    public GetNextDemandeRecouvTasklet getNextDemandeRecouvTasklet() { return new GetNextDemandeRecouvTasklet(minHour, maxHour); }
    @Bean
    public LireLigneFichierTasklet lireLigneFichierTasklet() { return new LireLigneFichierTasklet(); }
    @Bean
    public AuthentifierSurSudocTasklet authentifierSurSudocTasklet()
    {
        return new AuthentifierSurSudocTasklet();
    }
    @Bean
    public GenererFichierTasklet genererFichierTasklet() { return new GenererFichierTasklet(); }
    @Bean
    public VerifierParamsTasklet verifierParamsTasklet() { return new VerifierParamsTasklet(); }

    @Bean
    public ExportStatistiquesTasklet exportStatistiquesTasklet() { return new ExportStatistiquesTasklet(); }


    //Archivage automatique des demandes
    @Bean
    ChangeInArchivedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet changeInArchivedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet(){
        return new ChangeInArchivedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet();
    }
    @Bean
    ChangeInArchivedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet changeInArchivedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet(){
        return new ChangeInArchivedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet();
    }
    @Bean
    ChangeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet changeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet(){
        return new ChangeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet();
    }
    //Passage en statut supprimé automatique des demandes
    @Bean
    ChangeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet changeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet(){
        return new ChangeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet();
    }
    @Bean
    ChangeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet changeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet(){
        return new ChangeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet();
    }
    @Bean
    ChangeInDeletedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet changeInDeletedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet(){
        return new ChangeInDeletedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet();
    }
    //Suppression définitive des demandes
    @Bean
    DeleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet deleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet(){
        return new DeleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet();
    }
    @Bean
    DeleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet deleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet(){
        return new DeleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet();
    }
    @Bean
    DeleteAllDemandesRecouvInDeletedStatusForMoreThanSevenMonthsTasklet deleteAllDemandesRecouvInDeletedStatusForMoreThanSevenMonthsTasklet(){
        return new DeleteAllDemandesRecouvInDeletedStatusForMoreThanSevenMonthsTasklet();
    }

    // ---------- STEP --------------------------------------------

    @Bean
    public Step stepRecupererNextDemandeModif(JobRepository jobRepository, Tasklet getNextDemandeModifTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeModif", jobRepository).allowStartIfComplete(true)
                .tasklet(getNextDemandeModifTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeExemp(JobRepository jobRepository, Tasklet getNextDemandeExempTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(getNextDemandeExempTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeRecouv(JobRepository jobRepository, Tasklet getNextDemandeRecouvTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepRecupererNextDemandeRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(getNextDemandeRecouvTasklet, transactionManager)
                .build();
    }
    // Steps pour lancement d'un traitement de modification de masse
    @Bean
    public Step stepLireLigneFichier(JobRepository jobRepository, Tasklet lireLigneFichierTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepLireLigneFichier", jobRepository).allowStartIfComplete(true)
                .tasklet(lireLigneFichierTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepAuthentifierSurSudoc(JobRepository jobRepository, Tasklet authentifierSurSudocTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepAuthentifierSurSudoc", jobRepository).allowStartIfComplete(true)
                .tasklet(authentifierSurSudocTasklet, transactionManager)
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
    public Step stepGenererFichier(JobRepository jobRepository, Tasklet genererFichierTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepGenererFichier", jobRepository).allowStartIfComplete(true)
                .tasklet(genererFichierTasklet, transactionManager)
                .build();
    }

    // Steps pour exports statistiques
    @Bean
    public Step stepVerifierParams(JobRepository jobRepository, Tasklet verifierParamsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepVerifierParams", jobRepository).allowStartIfComplete(true)
                .tasklet(verifierParamsTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step stepExportStatistiques(JobRepository jobRepository, Tasklet exportStatistiquesTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepExportStatistiques", jobRepository).allowStartIfComplete(true)
                .tasklet(exportStatistiquesTasklet, transactionManager)
                .build();
    }


    //Steps d'archivage automatique des demandes
    @Bean
    public Step stepArchivageAutomatiqueDemandesExemp(JobRepository jobRepository, Tasklet changeInArchivedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(changeInArchivedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesModif(JobRepository jobRepository, Tasklet changeInArchivedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesModif", jobRepository).allowStartIfComplete(true)
                .tasklet(changeInArchivedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesRecouv(JobRepository jobRepository, Tasklet changeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepArchivageAutomatiqueDemandesRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(changeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet, transactionManager)
                .build();
    }

    //Steps de placement en statut supprimé automatique des demandes
    @Bean
    public Step stepChangementStatutSupprimeDemandesExemp(JobRepository jobRepository, Tasklet changeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(changeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepChangementStatutSupprimeDemandesModif(JobRepository jobRepository, Tasklet changeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesModif", jobRepository).allowStartIfComplete(true)
                .tasklet(changeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepChangementStatutSupprimeDemandesRecouv(JobRepository jobRepository, Tasklet changeInDeletedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepChangementStatutSupprimeDemandesRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(changeInDeletedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet, transactionManager)
                .build();
    }

    //Steps de destruction en base de donnée des demandes
    @Bean
    public Step stepSuppresionDemandesExemp(JobRepository jobRepository, Tasklet deleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppresionDemandesExemp", jobRepository).allowStartIfComplete(true)
                .tasklet(deleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepSuppresionDemandesModif(JobRepository jobRepository, Tasklet deleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppresionDemandesModif", jobRepository).allowStartIfComplete(true)
                .tasklet(deleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet, transactionManager)
                .build();
    }
    @Bean
    public Step stepSuppresionDemandesRecouv(JobRepository jobRepository, Tasklet deleteAllDemandesRecouvInDeletedStatusForMoreThanSevenMonthsTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepSuppresionDemandesRecouv", jobRepository).allowStartIfComplete(true)
                .tasklet(deleteAllDemandesRecouvInDeletedStatusForMoreThanSevenMonthsTasklet, transactionManager)
                .build();
    }


    // ---------- JOB ---------------------------------------------

    // Job de lancement d'un traitement de modification
    @Bean
    public Job jobTraiterLigneFichier(JobRepository jobRepository, Step stepRecupererNextDemandeModif, Step stepLireLigneFichier, Step stepAuthentifierSurSudoc, Step stepGenererFichier, Step stepTraiterLigneFichier) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_MODIF_NAME, jobRepository ).incrementer(incrementer())
                .start(stepRecupererNextDemandeModif).on(Constant.FAILED).end()
                .from(stepRecupererNextDemandeModif).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepRecupererNextDemandeModif).on(Constant.COMPLETED).to(stepLireLigneFichier)
                .from(stepLireLigneFichier).on(Constant.FAILED).end()
                .from(stepLireLigneFichier).on(Constant.COMPLETED).to(stepAuthentifierSurSudoc)
                .from(stepAuthentifierSurSudoc).on(Constant.FAILED).end()
                .from(stepAuthentifierSurSudoc).on(Constant.COMPLETED).to(stepTraiterLigneFichier)
                .from(stepTraiterLigneFichier).on(Constant.FAILED).end()
                .from(stepTraiterLigneFichier).on(Constant.COMPLETED).to(stepGenererFichier)
                .build().build();
    }

    //job de lancement d'un traitement d'exemplarisation
    @Bean
    public Job jobTraiterLigneFichierExemp(JobRepository jobRepository, Step stepRecupererNextDemandeExemp, Step stepLireLigneFichier, Step stepAuthentifierSurSudoc, Step stepTraiterLigneFichier, Step stepGenererFichier) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_EXEMP_NAME, jobRepository).incrementer(incrementer())
                .start(stepRecupererNextDemandeExemp).on(Constant.FAILED).end()
                .from(stepRecupererNextDemandeExemp).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepRecupererNextDemandeExemp).on(Constant.COMPLETED).to(stepLireLigneFichier)
                .from(stepLireLigneFichier).on(Constant.FAILED).end()
                .from(stepLireLigneFichier).on(Constant.COMPLETED).to(stepAuthentifierSurSudoc)
                .from(stepAuthentifierSurSudoc).on(Constant.FAILED).end()
                .from(stepAuthentifierSurSudoc).on(Constant.COMPLETED).to(stepTraiterLigneFichier)
                .from(stepTraiterLigneFichier).on(Constant.FAILED).end()
                .from(stepTraiterLigneFichier).on(Constant.COMPLETED).to(stepGenererFichier)
                .build().build();
    }

    //job de lancement d'un test de recouvrement
    @Bean
    public Job jobTraiterLigneFichierRecouv(JobRepository jobRepository, Step stepRecupererNextDemandeRecouv, Step stepLireLigneFichier, Step stepAuthentifierSurSudoc, Step stepTraiterLigneFichier, Step stepGenererFichier) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_RECOU_NAME, jobRepository).incrementer(incrementer())
                .start(stepRecupererNextDemandeRecouv).on(Constant.FAILED).end()
                .from(stepRecupererNextDemandeRecouv).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepRecupererNextDemandeRecouv).on(Constant.COMPLETED).to(stepLireLigneFichier)
                .from(stepLireLigneFichier).on(Constant.FAILED).end()
                .from(stepLireLigneFichier).on(Constant.COMPLETED).to(stepAuthentifierSurSudoc)
                .from(stepAuthentifierSurSudoc).on(Constant.FAILED).end()
                .from(stepAuthentifierSurSudoc).on(Constant.COMPLETED).to(stepTraiterLigneFichier)
                .from(stepTraiterLigneFichier).on(Constant.FAILED).end()
                .from(stepTraiterLigneFichier).on(Constant.COMPLETED).to(stepGenererFichier)
                .build().build();
    }


    // Job d'export des statistiques mensuelles
    @Bean
    public Job jobExportStatistiques(JobRepository jobRepository, Step stepVerifierParams, Step stepExportStatistiques) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_EXPORT_STATISTIQUES_NAME, jobRepository).incrementer(incrementer())
                .start(stepVerifierParams).on(Constant.FAILED).end()
                .from(stepVerifierParams).on(Constant.COMPLETED).to(stepExportStatistiques)
                .build().build();
    }

    //Job d'archivage automatique de toutes les demandes en statut terminé dont la dernière modification à plus de trois mois
    @Bean
    public Job jobArchivageDemandes(JobRepository jobRepository, Step stepArchivageAutomatiqueDemandesExemp, Step stepArchivageAutomatiqueDemandesModif, Step stepArchivageAutomatiqueDemandesRecouv) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_ARCHIVAGE_DEMANDES_EN_BASE, jobRepository).incrementer(incrementer())
                .start(stepArchivageAutomatiqueDemandesExemp).on(Constant.FAILED).end()
                .from(stepArchivageAutomatiqueDemandesExemp).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepArchivageAutomatiqueDemandesExemp).on(Constant.COMPLETED).end()
                .from(stepArchivageAutomatiqueDemandesModif).on(Constant.FAILED).end()
                .from(stepArchivageAutomatiqueDemandesModif).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepArchivageAutomatiqueDemandesModif).on(Constant.COMPLETED).end()
                .from(stepArchivageAutomatiqueDemandesRecouv).on(Constant.FAILED).end()
                .from(stepArchivageAutomatiqueDemandesRecouv).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepArchivageAutomatiqueDemandesRecouv).on(Constant.COMPLETED).end()
                .build().build();
    }

    //Job de placement en statut supprimé de toutes les demandes en statut archivé dont ce statut à plus de trois mois
    @Bean Job jobSuppressionMaisConservationEnBaseDemandes(JobRepository jobRepository, Step stepChangementStatutSupprimeDemandesExemp, Step stepChangementStatutSupprimeDemandesModif, Step stepChangementStatutSupprimeDemandesRecouv) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_STATUT_SUPPRIME_DEMANDES_EN_BASE, jobRepository).incrementer(incrementer())
                .start(stepChangementStatutSupprimeDemandesExemp).on(Constant.FAILED).end()
                .from(stepChangementStatutSupprimeDemandesExemp).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepChangementStatutSupprimeDemandesExemp).on(Constant.COMPLETED).end()
                .from(stepChangementStatutSupprimeDemandesModif).on(Constant.FAILED).end()
                .from(stepChangementStatutSupprimeDemandesModif).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepChangementStatutSupprimeDemandesModif).on(Constant.COMPLETED).end()
                .from(stepChangementStatutSupprimeDemandesRecouv).on(Constant.FAILED).end()
                .from(stepChangementStatutSupprimeDemandesRecouv).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepChangementStatutSupprimeDemandesRecouv).on(Constant.COMPLETED).end()
                .build().build();
    }

    //Job de suppression définitive en base de donnée de toutes les demandes en statut supprimé, dont ce statut à plus de trois mois
    @Bean Job jobSuppressionDefinitiveDemandes(JobRepository jobRepository, Step stepSuppresionDemandesExemp, Step stepSuppresionDemandesModif, Step stepSuppresionDemandesRecouv) {
        return new JobBuilder(Constant.SPRING_BATCH_JOB_SUPPRESSION_DEMANDES_EN_BASE, jobRepository).incrementer(incrementer())
                .start(stepSuppresionDemandesExemp).on(Constant.FAILED).end()
                .from(stepSuppresionDemandesExemp).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepSuppresionDemandesExemp).on(Constant.COMPLETED).end()
                .from(stepSuppresionDemandesModif).on(Constant.FAILED).end()
                .from(stepSuppresionDemandesModif).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepSuppresionDemandesModif).on(Constant.COMPLETED).end()
                .from(stepSuppresionDemandesRecouv).on(Constant.FAILED).end()
                .from(stepSuppresionDemandesRecouv).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepSuppresionDemandesRecouv).on(Constant.COMPLETED).end()
                .build().build();
    }




    // ------------------ INCREMENTER ------------------
    protected JobParametersIncrementer incrementer() {
        return new TimeIncrementer();
    }

}
