import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaboratorioServiceImpl extends UnicastRemoteObject implements LaboratorioService {

    private Map<String, Equipo> equipos = new HashMap<>();

    public LaboratorioServiceImpl() throws RemoteException {
        equipos.put("EQ001", new Equipo("EQ001", "Osciloscopio", "Lab Electronica"));
        equipos.put("EQ002", new Equipo("EQ002", "Multimetro", "Lab Electronica"));
        equipos.put("EQ003", new Equipo("EQ003", "Microscopio", "Lab Fisica"));
        equipos.put("EQ004", new Equipo("EQ004", "Generador de funciones", "Lab Electronica"));
    }

    @Override
    public synchronized List<String> consultarEquipos() throws RemoteException {
        List<String> resultado = new ArrayList<>();
        for (Equipo equipo : equipos.values()) {
            resultado.add(equipo.toString());
        }
        return resultado;
    }

    @Override
    public synchronized String consultarEquipo(String codigo) throws RemoteException {
        Equipo equipo = equipos.get(codigo);
        if (equipo == null) {
            return "ERROR_EQUIPO_NO_EXISTE";
        }
        return equipo.toString();
    }

    @Override
    public synchronized boolean reservarEquipo(String codigo) throws RemoteException {
        Equipo equipo = equipos.get(codigo);
        if (equipo == null || equipo.isReservado()) {
            return false;
        }
        equipo.setReservado(true);
        return true;
    }

    @Override
    public synchronized boolean liberarEquipo(String codigo) throws RemoteException {
        Equipo equipo = equipos.get(codigo);
        if (equipo == null || !equipo.isReservado()) {
            return false;
        }
        equipo.setReservado(false);
        return true;
    }
}