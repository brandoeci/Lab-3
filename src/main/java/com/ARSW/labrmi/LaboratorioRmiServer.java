import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LaboratorioRmiServer {
    public static void main(String[] args) throws Exception {
        LaboratorioService service = new LaboratorioServiceImpl();
        Registry registry = LocateRegistry.createRegistry(24000);
        registry.rebind("laboratorioService", service);
        System.out.println("LaboratorioService RMI publicado en puerto 24000...");
    }
}