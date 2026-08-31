package cl.duoc.bankbatch.intereses;

import cl.duoc.bankbatch.config.BankProperties;
import cl.duoc.bankbatch.support.RegistroInvalidoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InteresProcessor implements ItemProcessor<CuentaCsv, CuentaInteres> {

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 110;

    private final BankProperties propiedades;
    private final Set<String> vistos = ConcurrentHashMap.newKeySet();

    public InteresProcessor(BankProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public CuentaInteres process(CuentaCsv item) {
        if (!vistos.add(item.clave())) {
            return null;
        }

        Long cuentaId = parseCuentaId(item.cuentaId());
        BigDecimal saldo = parseSaldo(item.saldo());
        TipoCuenta tipo = TipoCuenta.desde(item.tipo());

        List<String> observaciones = new ArrayList<>();
        Integer edad = parseEdad(item.edad(), observaciones);

        BigDecimal tasa = tasaDe(tipo);
        BigDecimal interes = saldo.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoFinal = saldo.add(interes).setScale(2, RoundingMode.HALF_UP);

        String nombre = item.nombre() == null ? "" : item.nombre().trim();
        if (nombre.isBlank() || nombre.equalsIgnoreCase("Unknown")) {
            nombre = "Titular no identificado";
            observaciones.add("nombre ausente en origen");
        }

        return new CuentaInteres(cuentaId, nombre, tipo, edad, saldo.setScale(2, RoundingMode.HALF_UP),
                tasa, interes, saldoFinal,
                observaciones.isEmpty() ? null : String.join("; ", observaciones));
    }

    private BigDecimal tasaDe(TipoCuenta tipo) {
        return switch (tipo) {
            case AHORRO -> propiedades.tasas().ahorro();
            case PRESTAMO -> propiedades.tasas().prestamo();
            case HIPOTECA -> propiedades.tasas().hipoteca();
        };
    }

    private Long parseCuentaId(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("cuenta_id", "vacio");
        }
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw new RegistroInvalidoException("cuenta_id", "no numerico: " + valor);
        }
    }

    private BigDecimal parseSaldo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("saldo", "vacio");
        }
        BigDecimal saldo;
        try {
            saldo = new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            throw new RegistroInvalidoException("saldo", "no numerico: " + valor);
        }
        if (saldo.signum() < 0) {
            throw new RegistroInvalidoException("saldo", "negativo: " + saldo);
        }
        return saldo;
    }

    private Integer parseEdad(String valor, List<String> observaciones) {
        if (valor == null || valor.isBlank()) {
            observaciones.add("edad ausente en origen");
            return null;
        }
        int edad;
        try {
            edad = Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            observaciones.add("edad no numerica (" + valor + ")");
            return null;
        }
        if (edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
            observaciones.add("edad fuera de rango (" + edad + ")");
            return null;
        }
        return edad;
    }
}
