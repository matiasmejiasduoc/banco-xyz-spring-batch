package cl.duoc.bankbatch.transacciones;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResumenDiarioTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(ResumenDiarioTasklet.class);

    private final EntityManager entityManager;

    public ResumenDiarioTasklet(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        entityManager.createQuery("delete from ResumenDiario").executeUpdate();

        List<Object[]> filas = entityManager.createQuery("""
                select t.fecha,
                       count(t),
                       sum(case when t.anomalia = true then 1 else 0 end),
                       coalesce(sum(case when t.anomalia = false
                                          and t.tipo = cl.duoc.bankbatch.transacciones.TipoTransaccion.CREDITO
                                         then t.monto else 0 end), 0),
                       coalesce(sum(case when t.anomalia = false
                                          and t.tipo = cl.duoc.bankbatch.transacciones.TipoTransaccion.DEBITO
                                         then t.monto else 0 end), 0),
                       coalesce(sum(case when t.anomalia = true then t.monto else 0 end), 0)
                from Transaccion t
                group by t.fecha
                order by t.fecha
                """, Object[].class).getResultList();

        for (Object[] fila : filas) {
            entityManager.persist(new ResumenDiario(
                    (LocalDate) fila[0],
                    (Long) fila[1],
                    (Long) fila[2],
                    (BigDecimal) fila[3],
                    (BigDecimal) fila[4],
                    (BigDecimal) fila[5]));
        }

        contribution.incrementWriteCount(filas.size());
        log.info("resumen diario generado para {} fechas", filas.size());
        return RepeatStatus.FINISHED;
    }
}
