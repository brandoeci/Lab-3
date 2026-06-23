import java.util.HashMap;
import java.util.Map;

public class SalonRepository {
    private Map<String, Salon> salones = new HashMap<>();

    public SalonRepository() {
        salones.put("E301", new Salon("E301"));
        salones.put("E302", new Salon("E302"));
        salones.put("E303", new Salon("E303"));
        salones.put("E304", new Salon("E304"));
    }

    public synchronized Salon findByCodigo(String codigo) {
        return salones.get(codigo);
    }

    public synchronized boolean reservar(String codigo) {
        Salon salon = salones.get(codigo);
        if (salon == null || salon.isReservado()) {
            return false;
        }
        salon.setReservado(true);
        return true;
    }

    public synchronized boolean liberar(String codigo) {
        Salon salon = salones.get(codigo);
        if (salon == null || !salon.isReservado()) {
            return false;
        }
        salon.setReservado(false);
        return true;
    }
}