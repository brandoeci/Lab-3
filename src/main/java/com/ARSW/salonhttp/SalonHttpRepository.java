import java.util.HashMap;
import java.util.Map;

public class SalonHttpRepository {
    private Map<String, SalonHttp> salones = new HashMap<>();

    public SalonHttpRepository() {
        salones.put("E301", new SalonHttp("E301"));
        salones.put("E302", new SalonHttp("E302"));
        salones.put("E303", new SalonHttp("E303"));
        salones.put("E304", new SalonHttp("E304"));
    }

    public synchronized SalonHttp findByCodigo(String codigo) {
        return salones.get(codigo);
    }

    public synchronized Map<String, SalonHttp> findAll() {
        return salones;
    }

    public synchronized boolean reservar(String codigo) {
        SalonHttp salon = salones.get(codigo);
        if (salon == null || salon.isReservado()) {
            return false;
        }
        salon.setReservado(true);
        return true;
    }

    public synchronized boolean liberar(String codigo) {
        SalonHttp salon = salones.get(codigo);
        if (salon == null || !salon.isReservado()) {
            return false;
        }
        salon.setReservado(false);
        return true;
    }
}