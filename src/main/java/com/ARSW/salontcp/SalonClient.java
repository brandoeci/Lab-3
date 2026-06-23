import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SalonClient {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Operaciones: CONSULTAR_SALON, RESERVAR_SALON, LIBERAR_SALON");
        System.out.print("Ingrese la operacion: ");
        String operacion = scanner.nextLine();

        System.out.print("Ingrese el codigo del salon (ej. E303): ");
        String codigo = scanner.nextLine();

        Socket socket = new Socket("127.0.0.1", 36000);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        out.println(operacion + "," + codigo);
        String response = in.readLine();
        System.out.println("Respuesta del servidor: " + response);

        in.close();
        out.close();
        socket.close();
    }
}