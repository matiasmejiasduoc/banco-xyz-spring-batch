package cl.duoc.bankbatch.transacciones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.duoc.bankbatch.support.RegistroInvalidoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransaccionProcessorTest {

    private final TransaccionProcessor procesador = new TransaccionProcessor();

    @Test
    void normalizaRegistroValido() {
        Transaccion resultado = procesador.process(new TransaccionCsv("1", "30-06-2024", "3000", "credito"));

        assertThat(resultado.getFecha()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.CREDITO);
        assertThat(resultado.isAnomalia()).isFalse();
    }

    @Test
    void marcaMontoNegativoComoAnomaliaSinDescartar() {
        Transaccion resultado = procesador.process(new TransaccionCsv("2", "2024-04-09", "-200", "debito"));

        assertThat(resultado.isAnomalia()).isTrue();
        assertThat(resultado.getMotivoAnomalia()).contains("monto negativo");
    }

    @Test
    void normalizaTipoNoReconocidoYLoMarca() {
        Transaccion resultado = procesador.process(new TransaccionCsv("3", "2024-04-09", "800", "invalid"));

        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.DESCONOCIDO);
        assertThat(resultado.isAnomalia()).isTrue();
    }

    @Test
    void descartaMontoVacio() {
        assertThatThrownBy(() -> procesador.process(new TransaccionCsv("4", "2024-04-09", "", "credito")))
                .isInstanceOf(RegistroInvalidoException.class);
    }
}
