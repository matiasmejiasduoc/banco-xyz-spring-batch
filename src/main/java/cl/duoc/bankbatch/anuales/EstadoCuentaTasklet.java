package cl.duoc.bankbatch.anuales;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Transactional;

public class EstadoCuentaTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(EstadoCuentaTasklet.class);

    private final EntityManager entityManager;
    private final long cuentaDesde;
    private final long cuentaHasta;

    public EstadoCuentaTasklet(EntityManager entityManager, long cuentaDesde, long cuentaHasta) {
        this.entityManager = entityManager;
        this.cuentaDesde = cuentaDesde;
        this.cuentaHasta = cuentaHasta;
    }

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        entityManager.createQuery(
                        "delete from EstadoCuentaAnual e where e.cuentaId between :desde and :hasta")
                .setParameter("desde", cuentaDesde)
                .setParameter("hasta", cuentaHasta)
                .executeUpdate();

        List<Object[]> filas = entityManager.createQuery("""
                select m.cuentaId,
                       m.anio,
                       count(m),
                       sum(case when m.anomalia = true then 1 else 0 end),
                       coalesce(sum(case when m.transaccion = cl.duoc.bankbatch.anuales.TipoMovimiento.DEPOSITO
                                         then m.monto else 0 end), 0),
                       coalesce(sum(case when m.transaccion <> cl.duoc.bankbatch.anuales.TipoMovimiento.DEPOSITO
                                         then m.monto else 0 end), 0)
                from MovimientoAnual m
                where m.cuentaId between :desde and :hasta
                group by m.cuentaId, m.anio
                order by m.cuentaId, m.anio
                """, Object[].class)
                .setParameter("desde", cuentaDesde)
                .setParameter("hasta", cuentaHasta)
                .getResultList();

        for (Object[] fila : filas) {
            entityManager.persist(new EstadoCuentaAnual(
                    (Long) fila[0],
                    (Integer) fila[1],
                    (Long) fila[2],
                    (Long) fila[3],
                    (BigDecimal) fila[4],
                    (BigDecimal) fila[5]));
        }

        contribution.incrementWriteCount(filas.size());
        log.info("particion cuentas {}-{}: {} estados generados", cuentaDesde, cuentaHasta, filas.size());
        return RepeatStatus.FINISHED;
    }
}
