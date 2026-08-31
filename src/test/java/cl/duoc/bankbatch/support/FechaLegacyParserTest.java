package cl.duoc.bankbatch.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FechaLegacyParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"2024-03-18", "2024/03/18", "18-03-2024", "18/03/2024"})
    void reconoceLosCuatroFormatosDelLegacy(String entrada) {
        assertThat(FechaLegacyParser.parse(entrada)).isEqualTo(LocalDate.of(2024, 3, 18));
    }

    @Test
    void rechazaFechaInexistente() {
        assertThatThrownBy(() -> FechaLegacyParser.parse("2024-13-01"))
                .isInstanceOf(RegistroInvalidoException.class);
    }

    @Test
    void rechazaFechaVacia() {
        assertThatThrownBy(() -> FechaLegacyParser.parse("  "))
                .isInstanceOf(RegistroInvalidoException.class);
    }
}
