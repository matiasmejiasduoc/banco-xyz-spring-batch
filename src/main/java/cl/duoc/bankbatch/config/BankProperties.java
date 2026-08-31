package cl.duoc.bankbatch.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank")
public record BankProperties(Input input, Scaling scaling, Tolerancia tolerancia, Tasas tasas) {

    public record Input(String transacciones, String intereses, String cuentasAnuales) {}

    public record Scaling(int chunkSize, int threads, int gridSize) {}

    public record Tolerancia(long skipLimit, long retryLimit) {}

    public record Tasas(BigDecimal ahorro, BigDecimal prestamo, BigDecimal hipoteca) {}
}
