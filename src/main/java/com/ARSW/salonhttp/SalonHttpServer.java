import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class SalonHttpServer {

    public static void main(String[] args) throws Exception {
        SalonHttpRepository repository = new SalonHttpRepository();

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/rooms", new RoomsHandler(repository));
        server.createContext("/rooms/reserve", new ReserveHandler(repository));
        server.createContext("/rooms/release", new ReleaseHandler(repository));
        server.setExecutor(null);
        server.start();

        System.out.println("SalonHttpServer escuchando en http://localhost:8081/rooms");
    }

    static class RoomsHandler implements HttpHandler {
        private SalonHttpRepository repository;

        public RoomsHandler(SalonHttpRepository repository) {
            this.repository = repository;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Metodo no permitido");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String id = extractParam(query, "id");

            String response;
            if (id == null) {
                StringBuilder sb = new StringBuilder("<html><body><h1>Salones</h1><ul>");
                for (Map.Entry<String, SalonHttp> entry : repository.findAll().entrySet()) {
                    SalonHttp salon = entry.getValue();
                    String estado = salon.isReservado() ? "RESERVADO" : "DISPONIBLE";
                    sb.append("<li>").append(salon.getCodigo()).append(": ").append(estado).append("</li>");
                }
                sb.append("</ul></body></html>");
                response = sb.toString();
                sendResponse(exchange, 200, response);
            } else {
                SalonHttp salon = repository.findByCodigo(id);
                if (salon == null) {
                    sendResponse(exchange, 404, "<html><body><h1>ERROR_SALON_NO_EXISTE</h1></body></html>");
                    return;
                }
                String estado = salon.isReservado() ? "SALON_RESERVADO" : "SALON_DISPONIBLE";
                response = "<html><body><h1>" + salon.getCodigo() + ": " + estado + "</h1></body></html>";
                sendResponse(exchange, 200, response);
            }
        }
    }

    static class ReserveHandler implements HttpHandler {
        private SalonHttpRepository repository;

        public ReserveHandler(SalonHttpRepository repository) {
            this.repository = repository;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Metodo no permitido");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String id = extractParam(query, "id");

            if (id == null) {
                sendResponse(exchange, 400, "ERROR_OPERACION_INVALIDA");
                return;
            }

            SalonHttp salon = repository.findByCodigo(id);
            if (salon == null) {
                sendResponse(exchange, 404, "ERROR_SALON_NO_EXISTE");
                return;
            }

            boolean exito = repository.reservar(id);
            String response = exito ? "RESERVA_EXITOSA" : "SALON_RESERVADO";
            sendResponse(exchange, 200, response);
        }
    }

    static class ReleaseHandler implements HttpHandler {
        private SalonHttpRepository repository;

        public ReleaseHandler(SalonHttpRepository repository) {
            this.repository = repository;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Metodo no permitido");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String id = extractParam(query, "id");

            if (id == null) {
                sendResponse(exchange, 400, "ERROR_OPERACION_INVALIDA");
                return;
            }

            SalonHttp salon = repository.findByCodigo(id);
            if (salon == null) {
                sendResponse(exchange, 404, "ERROR_SALON_NO_EXISTE");
                return;
            }

            boolean exito = repository.liberar(id);
            String response = exito ? "LIBERACION_EXITOSA" : "SALON_DISPONIBLE";
            sendResponse(exchange, 200, response);
        }
    }

    private static String extractParam(String query, String paramName) {
        if (query == null) {
            return null;
        }
        String[] pares = query.split("&");
        for (String par : pares) {
            String[] kv = par.split("=");
            if (kv.length == 2 && kv[0].equals(paramName)) {
                return kv[1];
            }
        }
        return null;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.sendResponseHeaders(statusCode, body.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(body.getBytes());
        os.close();
    }
}