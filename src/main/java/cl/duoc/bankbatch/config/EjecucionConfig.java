package cl.duoc.bankbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class EjecucionConfig {

    @Bean
    public AsyncTaskExecutor batchTaskExecutor(BankProperties propiedades) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("batch-");
        executor.setConcurrencyLimit(propiedades.scaling().threads());
        return executor;
    }
}
