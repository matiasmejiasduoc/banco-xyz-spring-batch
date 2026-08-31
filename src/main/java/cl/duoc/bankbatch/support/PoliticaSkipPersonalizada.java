package cl.duoc.bankbatch.support;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;

public class PoliticaSkipPersonalizada implements SkipPolicy {

    private final long limite;

    public PoliticaSkipPersonalizada(long limite) {
        this.limite = limite;
    }

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (t instanceof TransientDataAccessException || t instanceof DataAccessResourceFailureException) {
            return false;
        }
        boolean recuperable = t instanceof RegistroInvalidoException
                || t instanceof FlatFileParseException
                || t instanceof NumberFormatException;
        if (!recuperable) {
            return false;
        }
        if (skipCount >= limite) {
            throw new SkipLimitExceededException(limite, t);
        }
        return true;
    }
}
