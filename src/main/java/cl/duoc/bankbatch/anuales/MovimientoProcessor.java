package cl.duoc.bankbatch.anuales;

import cl.duoc.bankbatch.support.FechaLegacyParser;
import cl.duoc.bankbatch.support.RegistroInvalidoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MovimientoProcessor implements ItemProcessor<MovimientoCsv, MovimientoAnual> {

    private static final String DESCRIPCION_POR_DEFECTO = "Sin descripcion informada";

    @Override
    public MovimientoAnual process(MovimientoCsv item) {
        Long cuentaId = parseCuentaId(item.cuentaId());
        LocalDate fecha = FechaLegacyParser.parse(item.fecha());
        TipoMovimiento transaccion = TipoMovimiento.desde(item.transaccion());
        BigDecimal monto = parseMonto(item.monto());

        List<String> anomalias = new ArrayList<>();
        if (monto.signum() < 0) {
            anomalias.add("monto negativo");
        } else if (monto.signum() == 0) {
            anomalias.add("monto cero");
        }

        String descripcion = item.descripcion() == null ? "" : item.descripcion().trim();
        if (descripcion.isBlank()) {
            descripcion = DESCRIPCION_POR_DEFECTO;
            anomalias.add("descripcion ausente");
        }

        return new MovimientoAnual(cuentaId, fecha, transaccion, monto.abs(), descripcion,
                !anomalias.isEmpty(), anomalias.isEmpty() ? null : String.join("; ", anomalias));
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
