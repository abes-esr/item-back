package fr.abes.item.webstats;

import fr.abes.item.constant.Constant;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor
public class JobExportStatistiquesCompletionListener extends JobExecutionListenerSupport {
    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.debug(Constant.JOB_EXPORT_END_FOR_PERIOD
                    + jobExecution.getExecutionContext().get("annee") + "/" + jobExecution.getExecutionContext().get("mois"));
        }
    }
}
