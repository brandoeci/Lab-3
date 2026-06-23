import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class LaboratorioRmiClient {
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 24000);
        LaboratorioService service = (LaboratorioService) registry.lookup("laboratorioService");

        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menu Inventario de Laboratorios ---");
            System.out.println("1. Consultar todos los equipos");
            System.out.println("2. Consultar un equipo por codigo");
            System.out.println("3. Reservar un equipo");
            System.out.println("4. Liberar un equipo");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opcion: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    List<String> equipos = service.consultarEquipos();
                    System.out.println("Equipos registrados:");
                    for (String e : equipos) {
                        System.out.println(" - " + e);
                    }
                    break;

                case "2":
                    System.out.print("Ingrese el codigo del equipo: ");
                    String codigoConsulta = scanner.nextLine();
                    System.out.println(service.consultarEquipo(codigoConsulta));
                    break;

                case "3":
                    System.out.print("Ingrese el codigo del equipo a reservar: ");
                    String codigoReserva = scanner.nextLine();
                    boolean exitoReserva = service.reservarEquipo(codigoReserva);
                    System.out.println(exitoReserva ? "RESERVA_EXITOSA" : "ERROR_NO_SE_PUDO_RESERVAR");
                    break;

                case "4":
                    System.out.print("Ingrese el codigo del equipo a liberar: ");
                    String codigoLiberar = scanner.nextLine();
                    boolean exitoLiberar = service.liberarEquipo(codigoLiberar);
                    System.out.println(exitoLiberar ? "LIBERACION_EXITOSA" : "ERROR_NO_SE_PUDO_LIBERAR");
                    break;

                case "5":
                    salir = true;
                    break;

                default:
                    System.out.println("Opcion invalida");
            }
        }

        System.out.println("Cliente finalizado.");
    }
}