package cl.duoc.bankbatch.anuales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimiento_anual", indexes = @Index(name = "idx_movimiento_cuenta", columnList = "cuentaId"))
public class MovimientoAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cuentaId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private int anio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento transaccion;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private boolean anomalia;

    @Column(length = 200)
    private String motivoAnomalia;

    protected MovimientoAnual() {
    }

    public MovimientoAnual(Long cuentaId, LocalDate fecha, TipoMovimiento transaccion, BigDecimal monto,
            String descripcion, boolean anomalia, String motivoAnomalia) {
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.anio = fecha.getYear();
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
        this.anomalia = anomalia;
        this.motivoAnomalia = motivoAnomalia;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public int getAnio() {
        return anio;
    }
}
