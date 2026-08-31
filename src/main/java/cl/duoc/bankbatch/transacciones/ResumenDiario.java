package cl.duoc.bankbatch.transacciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "resumen_transaccion_diaria")
public class ResumenDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(nullable = false)
    private long cantidadTransacciones;

    @Column(nullable = false)
    private long cantidadAnomalias;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoCredito;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoDebito;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoAnomalo;

    protected ResumenDiario() {
    }

    public ResumenDiario(LocalDate fecha, long cantidadTransacciones, long cantidadAnomalias,
            BigDecimal montoCredito, BigDecimal montoDebito, BigDecimal montoAnomalo) {
        this.fecha = fecha;
        this.cantidadTransacciones = cantidadTransacciones;
        this.cantidadAnomalias = cantidadAnomalias;
        this.montoCredito = montoCredito;
        this.montoDebito = montoDebito;
        this.montoAnomalo = montoAnomalo;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
