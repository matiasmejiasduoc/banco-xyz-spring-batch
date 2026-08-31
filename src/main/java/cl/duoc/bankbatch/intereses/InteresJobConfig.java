package cl.duoc.bankbatch.intereses;

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
public class InteresJobConfig {

    public static final String JOB = "calculoInteresesMensuales";

    @Bean
    public ItemStreamReader<CuentaCsv> cuentaReader(BankProperties propiedades) {
        var delegado = new FlatFileItemReaderBuilder<CuentaCsv>()
                .name("cuentaCsvReader")
                .resource(new DefaultResourceLoader().getResource(propiedades.input().intereses()))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
                .fieldSetMapper(fs -> new CuentaCsv(
                        fs.readRawString("cuenta_id"),
                        fs.readRawString("nombre"),
                        fs.readRawString("saldo"),
                        fs.readRawString("edad"),
                        fs.readRawString("tipo")))
                .build();
        return new SynchronizedItemStreamReader<>(delegado);
    }

    @Bean
    public JpaItemWriter<CuentaInteres> cuentaInteresWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<CuentaInteres>().entityManagerFactory(emf).build();
    }

    @Bean
    public Step calcularInteresesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemStreamReader<CuentaCsv> cuentaReader,
            InteresProcessor procesador,
            JpaItemWriter<CuentaInteres> cuentaInteresWriter,
            AsyncTaskExecutor batchTaskExecutor,
            RegistroRechazadoRepository rechazados,
            BankProperties propiedades) {

        return new StepBuilder("calcularInteresesStep", jobRepository)
                .<CuentaCsv, CuentaInteres>chunk(propiedades.scaling().chunkSize())
                .transactionManager(transactionManager)
                .reader(cuentaReader)
                .processor(procesador)
                .writer(cuentaInteresWriter)
                .faultTolerant()
                .skipPolicy(new PoliticaSkipPersonalizada(propiedades.tolerancia().skipLimit()))
                .retry(TransientDataAccessException.class)
                .retryLimit(propiedades.tolerancia().retryLimit())
                .skipListener(new AuditoriaSkipListener<CuentaCsv, CuentaInteres>(JOB, rechazados))
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Job calculoInteresesMensualesJob(JobRepository jobRepository,
            Step calcularInteresesStep,
            MetricasJobListener metricas) {
        return new JobBuilder(JOB, jobRepository)
                .listener(metricas)
                .start(calcularInteresesStep)
                .build();
    }
}
