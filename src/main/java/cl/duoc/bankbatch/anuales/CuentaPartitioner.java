package cl.duoc.bankbatch.anuales;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class CuentaPartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    public CuentaPartitioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Long minimo = jdbcTemplate.queryForObject("select min(cuenta_id) from movimiento_anual", Long.class);
        Long maximo = jdbcTemplate.queryForObject("select max(cuenta_id) from movimiento_anual", Long.class);

        Map<String, ExecutionContext> particiones = new HashMap<>();
        if (minimo == null || maximo == null) {
            return particiones;
        }

        long total = maximo - minimo + 1;
        long particionesReales = Math.min(gridSize, total);
        long tamano = (long) Math.ceil((double) total / particionesReales);

        long desde = minimo;
        int indice = 0;
        while (desde <= maximo) {
            long hasta = Math.min(desde + tamano - 1, maximo);
            ExecutionContext contexto = new ExecutionContext();
            contexto.putLong("cuentaDesde", desde);
            contexto.putLong("cuentaHasta", hasta);
            particiones.put("particion" + indice, contexto);
            desde = hasta + 1;
            indice++;
        }
        return particiones;
    }
}
