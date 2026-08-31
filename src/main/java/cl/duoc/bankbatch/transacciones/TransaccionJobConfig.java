package cl.duoc.bankbatch.transacciones;

import cl.duoc.bankbatch.config.BankProperties;
import cl.duoc.bankbatch.support.AuditoriaSkipListener;
import cl.duoc.bankbatch.support.MetricasJobListener;
import cl.duoc.bankbatch.support.PoliticaSkipPersonalizada;
import cl.duoc.bankbatch.support.RegistroRechazadoRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransaccionJobConfig {

    public static final String JOB = "reporteTransaccionesDiarias";

    @Bean
    public ItemStreamReader<TransaccionCsv> transaccionReader(BankProperties propiedades) {
        var delegado = new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionCsvReader")
                .resource(new DefaultResourceLoader().getResource(propiedades.input().transacciones()))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fs -> new TransaccionCsv(
                        fs.readRawString("id"),
                        fs.readRawString("fecha"),
                        fs.readRawString("monto"),
                        fs.readRawString("tipo")))
                .build();

        return new SynchronizedItemStreamReader<>(delegado);
    }

    @Bean
    public JpaItemWriter<Transaccion> transaccionWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<Transaccion>().entityManagerFactory(emf).build();
    }

    @Bean
    public Step cargarTransaccionesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemStreamReader<TransaccionCsv> transaccionReader,
            TransaccionProcessor procesador,
            JpaItemWriter<Transaccion> transaccionWriter,
            AsyncTaskExecutor batchTaskExecutor,
            RegistroRechazadoRepository rechazados,
            BankProperties propiedades) {

        return new StepBuilder("cargarTransaccionesStep", jobRepository)
                .<TransaccionCsv, Transaccion>chunk(propiedades.scaling().chunkSize())
                .transactionManager(transactionManager)
                .reader(transaccionReader)
                .processor(procesador)
                .writer(transaccionWriter)
                .faultTolerant()
                .skipPolicy(new PoliticaSkipPersonalizada(propiedades.tolerancia().skipLimit()))
                .retry(TransientDataAccessException.class)
                .retryLimit(propiedades.tolerancia().retryLimit())
                .skipListener(new AuditoriaSkipListener<TransaccionCsv, Transaccion>(JOB, rechazados))
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Step resumenDiarioStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ResumenDiarioTasklet tasklet) {
        return new StepBuilder("resumenDiarioStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job reporteTransaccionesDiariasJob(JobRepository jobRepository,
            Step cargarTransaccionesStep,
            Step resumenDiarioStep,
            MetricasJobListener metricas) {
        return new JobBuilder(JOB, jobRepository)
                .listener(metricas)
                .start(cargarTransaccionesStep)
                .next(resumenDiarioStep)
                .build();
    }
}
