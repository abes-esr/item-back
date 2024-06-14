package fr.abes.item.batch;

import fr.abes.item.core.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;

@Slf4j
public class LogTime {
    public static void logDebutTraitement(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().put("debut", System.currentTimeMillis());
    }

    public static void logFinTraitement(StepExecution stepExecution) {
        long dureeMs = System.currentTimeMillis() - (long) stepExecution.getJobExecution().getExecutionContext().get("debut");
        log.warn(Constant.SPRING_BATCH_TOTAL_TIME_EXECUTION_MILLISECONDS + dureeMs);
        int dureeMinutes = (int) ((dureeMs / (1000 * 60)) % 60);
        log.warn(Constant.SPRING_BATCH_TOTAL_TIME_EXECUTION_MINUTES + dureeMinutes);
    }

    private LogTime(){}
}
