package cl.duoc.bankbatch.anuales;

import cl.duoc.bankbatch.support.RegistroInvalidoException;
import java.text.Normalizer;
import java.util.Locale;

public enum TipoMovimiento {
    DEPOSITO,
    RETIRO,
    COMPRA,
    PAGO;

    public static TipoMovimiento desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("transaccion", "vacia");
        }
        String base = Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return switch (base) {
            case "deposito" -> DEPOSITO;
            case "retiro" -> RETIRO;
            case "compra" -> COMPRA;
            case "pago" -> PAGO;
            default -> throw new RegistroInvalidoException("transaccion", "no reconocida: " + valor);
        };
    }

    public boolean esIngreso() {
        return this == DEPOSITO;
    }
}
