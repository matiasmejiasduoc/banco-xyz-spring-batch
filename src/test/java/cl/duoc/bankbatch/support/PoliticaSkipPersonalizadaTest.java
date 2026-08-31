package cl.duoc.bankbatch.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;

class PoliticaSkipPersonalizadaTest {

    private final PoliticaSkipPersonalizada politica = new PoliticaSkipPersonalizada(10);

    @Test
    void saltaDatosDeNegocioInvalidos() {
        assertThat(politica.shouldSkip(new RegistroInvalidoException("monto", "vacio"), 0)).isTrue();
    }

    @Test
    void noSaltaFallosDeInfraestructura() {
        assertThat(politica.shouldSkip(new DataAccessResourceFailureException("bd caida"), 0)).isFalse();
        assertThat(politica.shouldSkip(new QueryTimeoutException("timeout"), 0)).isFalse();
    }

    @Test
    void noSaltaExcepcionesDesconocidas() {
        assertThat(politica.shouldSkip(new IllegalStateException("inesperado"), 0)).isFalse();
    }

    @Test
    void fallaAlSuperarElLimite() {
        assertThatThrownBy(() -> politica.shouldSkip(new RegistroInvalidoException("monto", "vacio"), 10))
                .isInstanceOf(SkipLimitExceededException.class);
    }
}
