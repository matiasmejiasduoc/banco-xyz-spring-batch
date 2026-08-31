package cl.duoc.bankbatch.transacciones;

import java.util.Locale;

public enum TipoTransaccion {
    CREDITO,
    DEBITO,
    DESCONOCIDO;

    public static TipoTransaccion desde(String valor) {
        if (valor == null) {
            return DESCONOCIDO;
        }
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "credito", "crédito" -> CREDITO;
            case "debito", "débito" -> DEBITO;
            default -> DESCONOCIDO;
        };
    }
}
