package cl.duoc.bankbatch.transacciones;

public record TransaccionCsv(String id, String fecha, String monto, String tipo) {

    @Override
    public String toString() {
        return "id=%s fecha=%s monto=%s tipo=%s".formatted(id, fecha, monto, tipo);
    }
}
