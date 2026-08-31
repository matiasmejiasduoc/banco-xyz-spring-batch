package cl.duoc.bankbatch.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class AuditoriaSkipListener<I, O> implements SkipListener<I, O> {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaSkipListener.class);

    private final String proceso;
    private final RegistroRechazadoRepository repositorio;

    public AuditoriaSkipListener(String proceso, RegistroRechazadoRepository repositorio) {
        this.proceso = proceso;
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSkipInRead(Throwable t) {
        registrar("lectura", t, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSkipInProcess(I item, Throwable t) {
        registrar("proceso", t, item);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSkipInWrite(O item, Throwable t) {
        registrar("escritura", t, item);
    }

    private void registrar(String etapa, Throwable t, Object item) {
        String campo = t instanceof RegistroInvalidoException rie ? rie.getCampo() : etapa;
        String contenido = item == null ? null : recortar(item.toString());
        repositorio.save(new RegistroRechazado(proceso, campo, recortar(t.getMessage()), contenido));
        log.debug("[{}] descartado en {}: {}", proceso, etapa, t.getMessage());
    }

    private String recortar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.length() > 500 ? valor.substring(0, 500) : valor;
    }
}
