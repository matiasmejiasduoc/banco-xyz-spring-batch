package cl.duoc.bankbatch.transacciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaccion")
public class Transaccion {

    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransaccion tipo;

    @Column(nullable = false)
    private boolean anomalia;

    @Column(length = 200)
    private String motivoAnomalia;

    protected Transaccion() {
    }

    public Transaccion(Long id, LocalDate fecha, BigDecimal monto, TipoTransaccion tipo,
            boolean anomalia, String motivoAnomalia) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.anomalia = anomalia;
        this.motivoAnomalia = motivoAnomalia;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public boolean isAnomalia() {
        return anomalia;
    }

    public String getMotivoAnomalia() {
        return motivoAnomalia;
    }
}
