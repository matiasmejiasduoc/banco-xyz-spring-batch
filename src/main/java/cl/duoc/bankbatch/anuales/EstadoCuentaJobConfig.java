package cl.duoc.bankbatch.anuales;

import cl.duoc.bankbatch.config.BankProperties;
import cl.duoc.bankbatch.support.AuditoriaSkipListener;
import cl.duoc.bankbatch.support.MetricasJobListener;
import cl.duoc.bankbatch.support.PoliticaSkipPersonalizada;
import cl.duoc.bankbatch.support.RegistroRechazadoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EstadoCuentaJobConfig {

    public static final String JOB = "generacionEstadosAnuales";

    @Bean
    public ItemStreamReader<MovimientoCsv> movimientoReader(BankProperties propiedades) {
        return new FlatFileItemReaderBuilder<MovimientoCsv>()
                .name("movimientoCsvReader")
                .resource(new DefaultResourceLoader().getResource(propiedades.input().cuentasAnuales()))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion")
                .fieldSetMapper(fs -> new MovimientoCsv(
                        fs.readRawString("cuenta_id"),
                        fs.readRawString("fecha"),
                        fs.readRawString("transaccion"),
                        fs.readRawString("monto"),
                        fs.readRawString("descripcion")))
                .build();
    }

    @Bean
    public JpaItemWriter<MovimientoAnual> movimientoWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<MovimientoAnual>().entityManagerFactory(emf).build();
    }

    @Bean
    public Step cargarMovimientosStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemStreamReader<MovimientoCsv> movimientoReader,
            MovimientoProcessor procesador,
            JpaItemWriter<MovimientoAnual> movimientoWriter,
            RegistroRechazadoRepository rechazados,
            BankProperties propiedades) {

        return new StepBuilder("cargarMovimientosStep", jobRepository)
                .<MovimientoCsv, MovimientoAnual>chunk(propiedades.scaling().chunkSize())
                .transactionManager(transactionManager)
                .reader(movimientoReader)
                .processor(procesador)
                .writer(movimientoWriter)
                .faultTolerant()
                .skipPolicy(new PoliticaSkipPersonalizada(propiedades.tolerancia().skipLimit()))
                .retry(TransientDataAccessException.class)
                .retryLimit(propiedades.tolerancia().retryLimit())
                .skipListener(new AuditoriaSkipListener<MovimientoCsv, MovimientoAnual>(JOB, rechazados))
                .build();
    }

    @Bean
    @StepScope
    public Tasklet estadoCuentaTasklet(EntityManager entityManager,
            @Value("#{stepExecutionContext['cuentaDesde']}") Long cuentaDesde,
            @Value("#{stepExecutionContext['cuentaHasta']}") Long cuentaHasta) {
        return new EstadoCuentaTasklet(entityManager, cuentaDesde, cuentaHasta);
    }

    @Bean
    public Step estadoCuentaWorkerStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Tasklet estadoCuentaTasklet) {
        return new StepBuilder("estadoCuentaWorkerStep", jobRepository)
                .tasklet(estadoCuentaTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step estadoCuentaParticionadoStep(JobRepository jobRepository,
            Step estadoCuentaWorkerStep,
            JdbcTemplate jdbcTemplate,
            AsyncTaskExecutor batchTaskExecutor,
            BankProperties propiedades) {

        return new StepBuilder("estadoCuentaParticionadoStep", jobRepository)
                .partitioner(estadoCuentaWorkerStep.getName(), new CuentaPartitioner(jdbcTemplate))
                .step(estadoCuentaWorkerStep)
                .gridSize(propiedades.scaling().gridSize())
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Job generacionEstadosAnualesJob(JobRepository jobRepository,
            Step cargarMovimientosStep,
            Step estadoCuentaParticionadoStep,
            MetricasJobListener metricas) {
        return new JobBuilder(JOB, jobRepository)
                .listener(metricas)
                .start(cargarMovimientosStep)
                .next(estadoCuentaParticionadoStep)
                .build();
    }
}
