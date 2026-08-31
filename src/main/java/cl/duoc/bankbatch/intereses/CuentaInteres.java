package cl.duoc.bankbatch.intereses;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cuenta_interes")
public class CuentaInteres {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cuentaId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;

    @Column
    private Integer edad;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoInicial;

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal tasaAplicada;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal interesCalculado;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoFinal;

    @Column(length = 200)
    private String observacion;

    protected CuentaInteres() {
    }

    public CuentaInteres(Long cuentaId, String nombre, TipoCuenta tipo, Integer edad, BigDecimal saldoInicial,
            BigDecimal tasaAplicada, BigDecimal interesCalculado, BigDecimal saldoFinal, String observacion) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.tipo = tipo;
        this.edad = edad;
        this.saldoInicial = saldoInicial;
        this.tasaAplicada = tasaAplicada;
        this.interesCalculado = interesCalculado;
        this.saldoFinal = saldoFinal;
        this.observacion = observacion;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }
}
