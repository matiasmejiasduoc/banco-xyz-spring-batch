package cl.duoc.bankbatch.support;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_rechazado")
public class RegistroRechazado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String proceso;

    @Column(length = 60)
    private String campo;

    @Column(length = 500)
    private String motivo;

    @Column(length = 1000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime detectadoEn;

    protected RegistroRechazado() {
    }

    public RegistroRechazado(String proceso, String campo, String motivo, String contenido) {
        this.proceso = proceso;
        this.campo = campo;
        this.motivo = motivo;
        this.contenido = contenido;
        this.detectadoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getProceso() {
        return proceso;
    }

    public String getCampo() {
        return campo;
    }

    public String getMotivo() {
        return motivo;
    }
}
