package fr.abes.item;

import fr.abes.item.constant.Constant;
import fr.abes.item.mail.impl.Mailer;
import fr.abes.item.restart.SelectJobsToRestartTasklet;
import fr.abes.item.service.service.ServiceProvider;
import fr.abes.item.traitement.*;
import fr.abes.item.traitement.model.LigneFichierDtoModif;
import fr.abes.item.webstats.ExportStatistiquesTasklet;
import fr.abes.item.webstats.VerifierParamsTasklet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;

@Slf4j
@Configuration
@EnableRetry
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    protected JobBuilderFactory jobs;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("kopyaJdbcTemplate")
    @SuppressWarnings("squid:S3305")
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ServiceProvider service;


    @Bean
    public BatchConfigurer configurer(EntityManagerFactory entityManagerFactory){
        return new KopyaBatchConfigurer(entityManagerFactory);
    }

    // ---------- JOB ---------------------------------------------

    // Job de lancement d'un traitement de modification
    @Bean
    public Job jobTraiterLigneFichier(ItemReader itemReader, ItemProcessor itemProcessor, ItemWriter itemWriter) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START_MODIF);

        return jobs
                .get(Constant.SPRING_BATCH_JOB_MODIF_NAME).incrementer(incrementer())
                .start(stepRecupererNextDemandeModif()).on(Constant.FAILED).end()
                .from(stepRecupererNextDemandeModif()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepRecupererNextDemandeModif()).on(Constant.COMPLETED).to(stepLireLigneFichier())
                .from(stepLireLigneFichier()).on(Constant.FAILED).end()
                .from(stepLireLigneFichier()).on(Constant.COMPLETED).to(stepAuthentifierSurSudoc())
                .from(stepAuthentifierSurSudoc()).on(Constant.FAILED).end()
                .from(stepAuthentifierSurSudoc()).on(Constant.COMPLETED).to(stepTraiterLigneFichier(itemReader,itemProcessor,itemWriter))
                .from(stepTraiterLigneFichier(itemReader,itemProcessor,itemWriter)).on(Constant.FAILED).end()
                .from(stepTraiterLigneFichier(itemReader,itemProcessor,itemWriter)).on(Constant.COMPLETED).to(stepGenererFichier())
                .build().build();
    }

    //job de lancement d'un traitement d'exemplarisation
    @Bean
    public Job jobTraiterLigneFichierExemp(ItemReader itemReader, ItemProcessor itemProcessor, ItemWriter itemWriter) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START_EXEMP);

        return jobs
                .get(Constant.SPRING_BATCH_JOB_EXEMP_NAME).incrementer(incrementer())
                .start(stepRecupererNextDemandeExemp()).on(Constant.FAILED).end()
                .from(stepRecupererNextDemandeExemp()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepRecupererNextDemandeExemp()).on(Constant.COMPLETED).to(stepLireLigneFichier())
                .from(stepLireLigneFichier()).on(Constant.FAILED).end()
                .from(stepLireLigneFichier()).on(Constant.COMPLETED).to(stepAuthentifierSurSudoc())
                .from(stepAuthentifierSurSudoc()).on(Constant.FAILED).end()
                .from(stepAuthentifierSurSudoc()).on(Constant.COMPLETED).to(stepTraiterLigneFichier(itemReader, itemProcessor, itemWriter))
                .from(stepTraiterLigneFichier(itemReader, itemProcessor, itemWriter)).on(Constant.FAILED).end()
                .from(stepTraiterLigneFichier(itemReader, itemProcessor, itemWriter)).on(Constant.COMPLETED).to(stepGenererFichier())
                .build().build();
    }

    //job de lancement d'un test de recouvrement
    @Bean
    public Job jobTraiterLigneFichierRecouv(ItemReader itemReader, ItemProcessor itemProcessor, ItemWriter itemWriter) {
        log.info(Constant.JOB_TRAITER_LIGNE_FICHIER_START_RECOU);

        return jobs
                .get(Constant.SPRING_BATCH_JOB_RECOU_NAME).incrementer(incrementer())
                .start(stepRecupererNextDemandeRecouv()).on(Constant.FAILED).end()
                .from(stepRecupererNextDemandeRecouv()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepRecupererNextDemandeRecouv()).on(Constant.COMPLETED).to(stepLireLigneFichier())
                .from(stepLireLigneFichier()).on(Constant.FAILED).end()
                .from(stepLireLigneFichier()).on(Constant.COMPLETED).to(stepAuthentifierSurSudoc())
                .from(stepAuthentifierSurSudoc()).on(Constant.FAILED).end()
                .from(stepAuthentifierSurSudoc()).on(Constant.COMPLETED).to(stepTraiterLigneFichier(itemReader, itemProcessor, itemWriter))
                .from(stepTraiterLigneFichier(itemReader, itemProcessor, itemWriter)).on(Constant.FAILED).end()
                .from(stepTraiterLigneFichier(itemReader, itemProcessor, itemWriter)).on(Constant.COMPLETED).to(stepGenererFichier())
                .build().build();
    }

    // Job de relance des Jobs en état Unknown
    @Bean
    public Job jobRelanceJobsUnknown() {
        return jobs.get(Constant.SPRING_BATCH_JOB_RESTART_JOBS_UNKNOW).incrementer(incrementer())
                .start(stepSelectJobsToRestart())
                .build();
    }

    // Job d'export des statistiques mensuelles
    @Bean
    public Job jobExportStatistiques() {
        log.info(Constant.JOB_EXPORT_STATISTIQUES_START);

        return jobs
                .get(Constant.SPRING_BATCH_JOB_EXPORT_STATISTIQUES_NAME).incrementer(incrementer())
                .start(stepVerifierParams()).on(Constant.FAILED).end()
                .from(stepVerifierParams()).on(Constant.COMPLETED).to(stepExportStatistiques())
                .build().build();
    }

    //Job d'archivage automatique de toutes les demandes en statut terminé dont la dernière modification à plus de trois mois
    @Bean Job jobArchivageDemandes() {
        log.info("Archivage automatique des demandes d'exemplarisation, modification et recouvrement lancé");

        return jobs
                .get(Constant.SPRING_BATCH_JOB_ARCHIVAGE_DEMANDES_EN_BASE).incrementer(incrementer())
                .start(stepArchivageAutomatiqueDemandesExemp()).on(Constant.FAILED).end()
                .from(stepArchivageAutomatiqueDemandesExemp()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepArchivageAutomatiqueDemandesExemp()).on(Constant.COMPLETED).end()
                .from(stepArchivageAutomatiqueDemandesModif()).on(Constant.FAILED).end()
                .from(stepArchivageAutomatiqueDemandesModif()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepArchivageAutomatiqueDemandesModif()).on(Constant.COMPLETED).end()
                .from(stepArchivageAutomatiqueDemandesRecouv()).on(Constant.FAILED).end()
                .from(stepArchivageAutomatiqueDemandesRecouv()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepArchivageAutomatiqueDemandesRecouv()).on(Constant.COMPLETED).end()
                .build().build();
    }

    //Job de placement en statut supprimé de toutes les demandes en statut archivé dont ce statut à plus de trois mois
    @Bean Job jobSuppressionMaisConservationEnBaseDemandes() {
        log.info("Passage en statut supprimé (mais conservation en base) des demandes d'exemplarisation, modification et recouvrement lancé");

        return jobs
                .get(Constant.SPRING_BATCH_JOB_STATUT_SUPPRIME_DEMANDES_EN_BASE).incrementer(incrementer())
                .start(stepChangementStatutSupprimeDemandesExemp()).on(Constant.FAILED).end()
                .from(stepChangementStatutSupprimeDemandesExemp()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepChangementStatutSupprimeDemandesExemp()).on(Constant.COMPLETED).end()
                .from(stepChangementStatutSupprimeDemandesModif()).on(Constant.FAILED).end()
                .from(stepChangementStatutSupprimeDemandesModif()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepChangementStatutSupprimeDemandesModif()).on(Constant.COMPLETED).end()
                .from(stepChangementStatutSupprimeDemandesRecouv()).on(Constant.FAILED).end()
                .from(stepChangementStatutSupprimeDemandesRecouv()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepChangementStatutSupprimeDemandesRecouv()).on(Constant.COMPLETED).end()
                .build().build();
    }

    //Job de suppression définitive en base de donnée de toutes les demandes en statut supprimé, dont ce statut à plus de trois mois
    @Bean Job jobSuppressionDefinitiveDemandes() {
        log.info("Suppression déifnitive des demandes en base d'exemplarisation, modification et recouvrement");

        return jobs
                .get(Constant.SPRING_BATCH_JOB_SUPPRESSION_DEMANDES_EN_BASE).incrementer(incrementer())
                .start(stepSuppresionDemandesExemp()).on(Constant.FAILED).end()
                .from(stepSuppresionDemandesExemp()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepSuppresionDemandesExemp()).on(Constant.COMPLETED).end()
                .from(stepSuppresionDemandesModif()).on(Constant.FAILED).end()
                .from(stepSuppresionDemandesModif()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepSuppresionDemandesModif()).on(Constant.COMPLETED).end()
                .from(stepSuppresionDemandesRecouv()).on(Constant.FAILED).end()
                .from(stepSuppresionDemandesRecouv()).on(Constant.AUCUNE_DEMANDE).end()
                .from(stepSuppresionDemandesRecouv()).on(Constant.COMPLETED).end()
                .build().build();
    }

    // ---------- STEP --------------------------------------------

    @Bean
    public Step stepRecupererNextDemandeModif() {
        return stepBuilderFactory.get("stepRecupererNextDemandeModif").allowStartIfComplete(true)
                .tasklet(getNextDemandeModifTasklet())
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeExemp() {
        return stepBuilderFactory.get("stepRecupererNextDemandeExemp").allowStartIfComplete(true)
                .tasklet(getNextDemandeExempTasklet())
                .build();
    }
    @Bean
    public Step stepRecupererNextDemandeRecouv() {
        return stepBuilderFactory.get("stepRecupererNextDemandeRecouv").allowStartIfComplete(true)
                .tasklet(getNextDemandeRecouvTasklet())
                .build();
    }
    // Steps pour lancement d'un traitement de modification de masse
    @Bean
    public Step stepLireLigneFichier() {
        return stepBuilderFactory
                .get("stepLireLigneFichier").allowStartIfComplete(true)
                .tasklet(lireLigneFichierTasklet())
                .build();
    }
    @Bean
    public Step stepAuthentifierSurSudoc() {
        return stepBuilderFactory
                .get("stepAuthentifierSurSudoc").allowStartIfComplete(true)
                .tasklet(authentifierSurSudocTasklet())
                .build();
    }
    @Bean
    protected Step stepTraiterLigneFichier(ItemReader reader, ItemProcessor processor, ItemWriter writer) {
        return stepBuilderFactory
                .get("stepTraiterLigneFichier").<LigneFichierDtoModif, LigneFichierDtoModif> chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    @Bean
    public Step stepGenererFichier() {
        return stepBuilderFactory
                .get("stepGenererFichier").allowStartIfComplete(true)
                .tasklet(genererFichierTasklet())
                .build();
    }

    // Steps pour exports statistiques
    @Bean
    public Step stepVerifierParams() {
        return stepBuilderFactory
                .get("stepVerifierParams").allowStartIfComplete(true)
                .tasklet(verifierParamsTasklet())
                .build();
    }

    @Bean
    public Step stepExportStatistiques() {
        return stepBuilderFactory
                .get("stepExportStatistiques").allowStartIfComplete(true)
                .tasklet(exportStatistiquesTasklet())
                .build();
    }

    @Bean
    public Step stepSelectJobsToRestart() {
        return stepBuilderFactory
                .get("stepSelectJobsToRestart").allowStartIfComplete(true)
                .tasklet(selectJobsToRestartTasklet())
                .build();
    }

    //Steps d'archivage automatique des demandes
    @Bean
    public Step stepArchivageAutomatiqueDemandesExemp() {
        return stepBuilderFactory
                .get("stepArchivageAutomatiqueDemandesExemp").allowStartIfComplete(true)
                .tasklet(changeInArchivedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet())
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesModif() {
        return stepBuilderFactory
                .get("stepArchivageAutomatiqueDemandesModif").allowStartIfComplete(true)
                .tasklet(changeInArchivedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet())
                .build();
    }
    @Bean
    public Step stepArchivageAutomatiqueDemandesRecouv() {
        return stepBuilderFactory
                .get("stepArchivageAutomatiqueDemandesRecouv").allowStartIfComplete(true)
                .tasklet(changeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet())
                .build();
    }

    //Steps de placement en statut supprimé automatique des demandes
    @Bean
    public Step stepChangementStatutSupprimeDemandesExemp() {
        return stepBuilderFactory
                .get("stepChangementStatutSupprimeDemandesExemp").allowStartIfComplete(true)
                .tasklet(changeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet())
                .build();
    }
    @Bean
    public Step stepChangementStatutSupprimeDemandesModif() {
        return stepBuilderFactory
                .get("stepChangementStatutSupprimeDemandesModif").allowStartIfComplete(true)
                .tasklet(changeInDeletedStatusAllDemandesModifFinishedForMoreThanThreeMonthsTasklet())
                .build();
    }
    @Bean
    public Step stepChangementStatutSupprimeDemandesRecouv() {
        return stepBuilderFactory
                .get("stepChangementStatutSupprimeDemandesRecouv").allowStartIfComplete(true)
                .tasklet(changeInDeletedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet())
                .build();
    }

    //Steps de destruction en base de donnée des demandes
    @Bean
    public Step stepSuppresionDemandesExemp() {
        return stepBuilderFactory
                .get("stepSuppresionDemandesExemp").allowStartIfComplete(true)
                .tasklet(deleteAllDemandesExempInDeletedStatusForMoreThanSevenMonthsTasklet())
                .build();
    }
    @Bean
    public Step stepSuppresionDemandesModif() {
        return stepBuilderFactory
                .get("stepSuppresionDemandesModif").allowStartIfComplete(true)
                .tasklet(deleteAllDemandesModifInDeletedStatusForMoreThanSevenMonthsTasklet())
                .build();
    }
    @Bean
    public Step stepSuppresionDemandesRecouv() {
        return stepBuilderFactory
                .get("stepSuppresionDemandesRecouv").allowStartIfComplete(true)
                .tasklet(deleteAllDemandesRecouvInDeletedStatusForMoreThanSevenMonthsTasklet())
                .build();
    }

    // ------------- TASKLETS -----------------------
    @Bean
    public GetNextDemandeModifTasklet getNextDemandeModifTasklet() { return new GetNextDemandeModifTasklet(); }
    @Bean
    public GetNextDemandeExempTasklet getNextDemandeExempTasklet() { return new GetNextDemandeExempTasklet(); }
    @Bean
    public GetNextDemandeRecouvTasklet getNextDemandeRecouvTasklet() { return new GetNextDemandeRecouvTasklet(); }
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

    @Bean
    public SelectJobsToRestartTasklet selectJobsToRestartTasklet() { return new SelectJobsToRestartTasklet(jdbcTemplate, service); }


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

    // ------------------ INCREMENTER ------------------
    protected JobParametersIncrementer incrementer() {
        return new TimeIncrementer();
    }

}
