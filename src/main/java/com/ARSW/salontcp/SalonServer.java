import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SalonServer {

    public static void main(String[] args) throws Exception {
        SalonRepository repository = new SalonRepository();
        ServerSocket serverSocket = new ServerSocket(36000);
        System.out.println("SalonServer TCP escuchando en puerto 36000...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = in.readLine();
            String response = processRequest(request, repository);
            out.println(response);

            in.close();
            out.close();
            clientSocket.close();
        }
    }

    private static String processRequest(String request, SalonRepository repository) {
        if (request == null || !request.contains(",")) {
            return "ERROR_OPERACION_INVALIDA";
        }

        String[] partes = request.split(",");
        if (partes.length != 2) {
            return "ERROR_OPERACION_INVALIDA";
        }

        String operacion = partes[0];
        String codigo = partes[1];

        Salon salon = repository.findByCodigo(codigo);

        switch (operacion) {
            case "CONSULTAR_SALON":
                if (salon == null) {
                    return "ERROR_SALON_NO_EXISTE";
                }
                return salon.isReservado() ? "SALON_RESERVADO" : "SALON_DISPONIBLE";

            case "RESERVAR_SALON":
                if (salon == null) {
                    return "ERROR_SALON_NO_EXISTE";
                }
                return repository.reservar(codigo) ? "RESERVA_EXITOSA" : "SALON_RESERVADO";

            case "LIBERAR_SALON":
                if (salon == null) {
                    return "ERROR_SALON_NO_EXISTE";
                }
                return repository.liberar(codigo) ? "LIBERACION_EXITOSA" : "SALON_DISPONIBLE";

            default:
                return "ERROR_OPERACION_INVALIDA";
        }
    }
}