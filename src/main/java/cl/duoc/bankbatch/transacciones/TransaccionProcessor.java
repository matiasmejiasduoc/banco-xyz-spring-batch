package cl.duoc.bankbatch.transacciones;

import cl.duoc.bankbatch.support.FechaLegacyParser;
import cl.duoc.bankbatch.support.RegistroInvalidoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TransaccionProcessor implements ItemProcessor<TransaccionCsv, Transaccion> {

    @Override
    public Transaccion process(TransaccionCsv item) {
        Long id = parseId(item.id());
        LocalDate fecha = FechaLegacyParser.parse(item.fecha());
        BigDecimal monto = parseMonto(item.monto());

        List<String> anomalias = new ArrayList<>();

        TipoTransaccion tipo = TipoTransaccion.desde(item.tipo());
        if (tipo == TipoTransaccion.DESCONOCIDO) {
            anomalias.add("tipo no reconocido (" + item.tipo() + ")");
        }

        if (monto.signum() < 0) {
            anomalias.add("monto negativo");
        } else if (monto.signum() == 0) {
            anomalias.add("monto cero");
        }

        if (fecha.isAfter(LocalDate.now())) {
            anomalias.add("fecha futura");
        }

        return new Transaccion(id, fecha, monto, tipo, !anomalias.isEmpty(),
                anomalias.isEmpty() ? null : String.join("; ", anomalias));
    }

    private Long parseId(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("id", "vacio");
        }
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw new RegistroInvalidoException("id", "no numerico: " + valor);
        }
    }

    private BigDecimal parseMonto(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegistroInvalidoException("monto", "vacio");
        }
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            throw new RegistroInvalidoException("monto", "no numerico: " + valor);
        }
    }
}
