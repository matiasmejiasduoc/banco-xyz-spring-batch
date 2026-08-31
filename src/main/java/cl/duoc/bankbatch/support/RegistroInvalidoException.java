package cl.duoc.bankbatch.support;

public class RegistroInvalidoException extends RuntimeException {

    private final String campo;

    public RegistroInvalidoException(String campo, String detalle) {
        super("campo '" + campo + "': " + detalle);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
