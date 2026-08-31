package cl.duoc.bankbatch.anuales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "estado_cuenta_anual",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cuentaId", "anio"}))
public class EstadoCuentaAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cuentaId;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private long cantidadMovimientos;

    @Column(nullable = false)
    private long cantidadAnomalias;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalIngresos;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalEgresos;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoNeto;

    protected EstadoCuentaAnual() {
    }

    public EstadoCuentaAnual(Long cuentaId, int anio, long cantidadMovimientos, long cantidadAnomalias,
            BigDecimal totalIngresos, BigDecimal totalEgresos) {
        this.cuentaId = cuentaId;
        this.anio = anio;
        this.cantidadMovimientos = cantidadMovimientos;
        this.cantidadAnomalias = cantidadAnomalias;
        this.totalIngresos = totalIngresos;
        this.totalEgresos = totalEgresos;
        this.saldoNeto = totalIngresos.subtract(totalEgresos);
    }
}
