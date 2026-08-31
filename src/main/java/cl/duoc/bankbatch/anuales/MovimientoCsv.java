package cl.duoc.bankbatch.anuales;

public record MovimientoCsv(String cuentaId, String fecha, String transaccion, String monto, String descripcion) {

    @Override
    public String toString() {
        return "cuenta_id=%s fecha=%s transaccion=%s monto=%s descripcion=%s"
                .formatted(cuentaId, fecha, transaccion, monto, descripcion);
    }
}
