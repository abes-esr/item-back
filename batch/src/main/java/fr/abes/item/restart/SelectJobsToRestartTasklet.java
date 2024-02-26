package fr.abes.item.restart;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.factory.StrategyFactory;
import fr.abes.item.service.impl.DemandeExempService;
import fr.abes.item.service.impl.DemandeModifService;
import fr.abes.item.service.impl.DemandeRecouvService;
import fr.abes.item.service.ReferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class SelectJobsToRestartTasklet implements Tasklet, StepExecutionListener {
    private final JdbcTemplate jdbcTemplate;

    private final DemandeModifService demandeModifService;

    private final DemandeExempService demandeExempService;

    private final DemandeRecouvService demandeRecouvService;

    private final ReferenceService referenceService;

    private StrategyFactory factory;

    @Value("${mail.admin}")
    private String mailAdmin;

    public SelectJobsToRestartTasklet(JdbcTemplate jdbcTemplate, DemandeModifService demandeModifService, DemandeExempService demandeExempService, DemandeRecouvService demandeRecouvService, ReferenceService referenceService, StrategyFactory factory) {
        this.jdbcTemplate = jdbcTemplate;
        this.demandeModifService = demandeModifService;
        this.demandeExempService = demandeExempService;
        this.demandeRecouvService = demandeRecouvService;
        this.referenceService = referenceService;
        this.factory = factory;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<String> jobExecutionsModif = findRunningJobExecutions(Constant.SPRING_BATCH_JOB_MODIF_NAME);
        List<Integer> listDemandes = new ArrayList<>();
        List<Integer> finalListDemandes1 = listDemandes;
        jobExecutionsModif.forEach(j -> {
            //on récupère le numéro de la demande de modif à modifier
            String numDemande = extractNumDemandeModif(j);
            //si le numéro de la demande a été trouvé et que la demande est en cours de traitement
            if (!numDemande.equals("") && demandeModifService.findById(Integer.parseInt(numDemande)).getEtatDemande().getId() == Constant.ETATDEM_ENCOURS) {
                //on repasse la demande en attente de traitement
                Demande demande = demandeModifService.findById(Integer.parseInt(numDemande));
                demande.setEtatDemande(referenceService.findEtatDemandeById(Constant.ETATDEM_ATTENTE));
                demandeModifService.save(demande);
                log.info("Redémarrage job sur la demande " + demande.getNumDemande());
                finalListDemandes1.add(demande.getNumDemande());
            }
        });
        envoiMail(listDemandes.stream().distinct().collect(Collectors.toList()), TYPE_DEMANDE.MODIF);

        listDemandes = new ArrayList<>();
        List<String> jobExecutionsExemp = findRunningJobExecutions(Constant.SPRING_BATCH_JOB_EXEMP_NAME);
        List<Integer> finalListDemandes2 = listDemandes;
        jobExecutionsExemp.forEach(j -> {
            //on récupère le numéro de la demande d'exemplarisation à modifier
            String numDemande = extractNumDemandeExemp(j);
            if (!numDemande.equals("") && demandeExempService.findById(Integer.parseInt(numDemande)).getEtatDemande().getId() == Constant.ETATDEM_ENCOURS) {
                //on repasse la demande en attente de traitement
                Demande demande = demandeExempService.findById(Integer.parseInt(numDemande));
                demande.setEtatDemande(referenceService.findEtatDemandeById(Constant.ETATDEM_ATTENTE));
                demandeExempService.save(demande);
                log.info("Redémarrage job sur la demande " + demande.getNumDemande());
                finalListDemandes2.add(demande.getNumDemande());
            }
        });
        envoiMail(listDemandes.stream().distinct().collect(Collectors.toList()), TYPE_DEMANDE.EXEMP);

        listDemandes = new ArrayList<>();
        List<String> jobExecutionsRecouv = findRunningJobExecutions(Constant.SPRING_BATCH_JOB_RECOU_NAME);
        List<Integer> finalListDemandes = listDemandes;
        jobExecutionsRecouv.forEach(j -> {
            //on récupère le numéro de la demande de recouvrement à modifier
            String numDemande = extractNumDemandeRecouv(j);
            if (!numDemande.equals("") && demandeRecouvService.findById(Integer.parseInt(numDemande)).getEtatDemande().getId() == Constant.ETATDEM_ENCOURS) {
                //on repasse la demande en attente de traitement
                Demande demande = demandeRecouvService.findById(Integer.parseInt(numDemande));
                demande.setEtatDemande(referenceService.findEtatDemandeById(Constant.ETATDEM_ATTENTE));
                demandeRecouvService.save(demande);
                log.info("Redémarrage job sur la demande " + demande.getNumDemande());
                finalListDemandes.add(demande.getNumDemande());
            }
        });
        envoiMail(listDemandes.stream().distinct().collect(Collectors.toList()), TYPE_DEMANDE.RECOUV);

        return RepeatStatus.FINISHED;
    }

    private void envoiMail(List<Integer> listDemandes, TYPE_DEMANDE type_demande) {
        if (listDemandes.size() > 0) {
            IMailer mailer = factory.getStrategy(IMailer.class, type_demande);
            log.info("envoi du mail à " + mailAdmin + " pour type " + type_demande);
            mailer.mailRestartJob(listDemandes);
        }
    }

    private String extractNumDemandeRecouv(String context) {
        return context.substring(context.indexOf("RECOUV\",\"id\":") + 13, context.indexOf("}}"));
    }

    private String extractNumDemandeExemp(String context) {
        return context.substring(context.indexOf("EXEMP\",\"id\":") + 12, context.indexOf("}}"));
    }

    private String extractNumDemandeModif(String context) {
        return context.substring(context.indexOf("MODIF\",\"id\":") + 12, context.indexOf("}}"));
    }

    private List<String> findRunningJobExecutions(String jobName) {
        //on récupère les jobs en cours d'exécution
        List<String> resultats = new ArrayList<>();
        Stream<JobExecutionDto> jobExecutionDto = jdbcTemplate.queryForStream("SELECT C.JOB_EXECUTION_ID as EXECUTION_ID, C.SERIALIZED_CONTEXT as CONTEXT from BATCH_JOB_EXECUTION E, BATCH_JOB_EXECUTION_CONTEXT C, BATCH_JOB_INSTANCE I where E.JOB_EXECUTION_ID=C.JOB_EXECUTION_ID AND E.JOB_INSTANCE_ID=I.JOB_INSTANCE_ID and I.JOB_NAME=? and E.START_TIME is not NULL and E.END_TIME is NULL AND E.STATUS='STARTED' and E.EXIT_CODE='UNKNOWN' order by E.JOB_EXECUTION_ID desc", new JobExecutionRowMapper(), jobName);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -10);
        jobExecutionDto.forEach(j -> {
            //sur le job trouvé, on vérifie s'il existe un step en cours d'exécution et qui n'a pas été mis à jour depuis 1 heure
            Integer steps = jdbcTemplate.queryForObject("select count(STEP_EXECUTION_ID) from BATCH_STEP_EXECUTION where JOB_EXECUTION_ID = ? and EXIT_CODE='EXECUTING' and END_TIME is null and LAST_UPDATED < ?", Integer.class, j.getJobExecutionId(), now.getTime());
            if (steps > 0) {
                //si on trouve encore un step qui respecte la condition, on retourne le contexte du job pour pouvoir retrouver le numéro de la demande à modifier
                resultats.add(j.getContext());
            }
        });
        return resultats;
    }

    private class JobExecutionRowMapper implements RowMapper<JobExecutionDto> {
        @Override
        public JobExecutionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            JobExecutionDto dto = new JobExecutionDto();

            dto.setJobExecutionId(rs.getLong("EXECUTION_ID"));
            dto.setContext(rs.getString("CONTEXT"));

            return dto;
        }
    }
}
