package cl.duoc.bankbatch.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);

    private final JobOperator jobOperator;
    private final Map<String, Job> jobs = new LinkedHashMap<>();
    private final ConfigurableApplicationContext contexto;

    public JobRunner(JobOperator jobOperator, List<Job> jobsDisponibles, ConfigurableApplicationContext contexto) {
        this.jobOperator = jobOperator;
        this.contexto = contexto;
        jobsDisponibles.forEach(job -> jobs.put(job.getName(), job));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> solicitados = args.containsOption("job")
                ? args.getOptionValues("job")
                : List.copyOf(jobs.keySet());

        int fallidos = 0;
        for (String nombre : solicitados) {
            Job job = jobs.get(nombre);
            if (job == null) {
                log.error("job desconocido: {} (disponibles: {})", nombre, jobs.keySet());
                fallidos++;
                continue;
            }
            JobExecution ejecucion = jobOperator.start(job, new JobParametersBuilder()
                    .addString("ejecucion", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters());
            if (ejecucion.getStatus().isUnsuccessful()) {
                fallidos++;
            }
        }

        if (fallidos > 0) {
            System.exit(1);
        }
        contexto.close();
    }
}
