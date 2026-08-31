package cl.duoc.bankbatch.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

public final class FechaLegacyParser {

    private static final List<DateTimeFormatter> FORMATOS = List.of(
            formato("uuuu-MM-dd"),
            formato("uuuu/MM/dd"),
            formato("dd-MM-uuuu"),
            formato("dd/MM/uuuu"));

    private FechaLegacyParser() {
    }

    private static DateTimeFormatter formato(String patron) {
        return DateTimeFormatter.ofPattern(patron).withResolverStyle(ResolverStyle.STRICT);
    }

    public static LocalDate parse(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("fecha", "vacia");
        }
        String limpio = valor.trim();
        for (DateTimeFormatter formato : FORMATOS) {
            try {
                return LocalDate.parse(limpio, formato);
            } catch (DateTimeParseException ignorada) {
                continue;
            }
        }
        throw new RegistroInvalidoException("fecha", "formato no reconocido o fecha inexistente: " + limpio);
    }
}
