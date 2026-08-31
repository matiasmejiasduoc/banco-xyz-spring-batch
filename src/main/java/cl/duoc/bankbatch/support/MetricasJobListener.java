package cl.duoc.bankbatch.support;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class MetricasJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(MetricasJobListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration total = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
        log.info("================ RESUMEN {} ================", jobExecution.getJobInstance().getJobName());
        log.info("parametros    : {}", jobExecution.getJobParameters());
        log.info("estado        : {}", jobExecution.getStatus());
        log.info("duracion total: {} ms", total.toMillis());
        for (StepExecution step : jobExecution.getStepExecutions()) {
            Duration paso = Duration.between(step.getStartTime(), step.getEndTime());
            log.info("  step {} -> leidos={} escritos={} descartados={} reintentos_lectura={} commits={} duracion={} ms",
                    step.getStepName(),
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount(),
                    step.getRollbackCount(),
                    step.getCommitCount(),
                    paso.toMillis());
        }
        log.info("===========================================");
    }
}
