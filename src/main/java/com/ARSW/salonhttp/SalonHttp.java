public class SalonHttp {
    private String codigo;
    private boolean reservado;

    public SalonHttp(String codigo) {
        this.codigo = codigo;
        this.reservado = false;
    }

    public String getCodigo() {
        return codigo;
    }

    public boolean isReservado() {
        return reservado;
    }

    public void setReservado(boolean reservado) {
        this.reservado = reservado;
    }
}