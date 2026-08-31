package cl.duoc.bankbatch.intereses;

import cl.duoc.bankbatch.support.RegistroInvalidoException;
import java.util.Locale;

public enum TipoCuenta {
    AHORRO,
    PRESTAMO,
    HIPOTECA;

    public static TipoCuenta desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("tipo", "vacio");
        }
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "ahorro" -> AHORRO;
            case "prestamo", "préstamo" -> PRESTAMO;
            case "hipoteca" -> HIPOTECA;
            default -> throw new RegistroInvalidoException("tipo", "no reconocido: " + valor);
        };
    }
}
