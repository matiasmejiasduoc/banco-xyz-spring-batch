package cl.duoc.bankbatch.intereses;

public record CuentaCsv(String cuentaId, String nombre, String saldo, String edad, String tipo) {

    public String clave() {
        return String.join("|", cuentaId, nombre, saldo, edad, tipo);
    }

    @Override
    public String toString() {
        return "cuenta_id=%s nombre=%s saldo=%s edad=%s tipo=%s".formatted(cuentaId, nombre, saldo, edad, tipo);
    }
}
