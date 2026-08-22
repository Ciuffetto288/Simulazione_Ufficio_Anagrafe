import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/** Bridge locale tra il processo Java e il server Rust dell'ecosistema. */
public final class EcosistemaBridge {
    private static final int PORT = 8090;

    private final AtomicReference<Cittadino> pendingCitizen = new AtomicReference<>();
    private HttpServer server;

    /** Avvia il bridge solo sull'interfaccia locale. */
    public synchronized void start() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
            server.createContext("/api/stato", this::handleState);
            server.setExecutor(Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "anagrafe-bridge");
                thread.setDaemon(true);
                return thread;
            }));
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Impossibile avviare il bridge locale", exception);
        }
    }

    /** Aggiorna il cittadino calcolato ma non ancora confermato dall'utente. */
    public void setPendingCitizen(Cittadino citizen) {
        pendingCitizen.set(citizen);
    }

    /** Cancella il risultato temporaneo dopo la conferma dell'utente. */
    public void clearPendingCitizen() {
        pendingCitizen.set(null);
    }

    /** Arresta il bridge quando termina l'applicazione Java. */
    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        pendingCitizen.set(null);
    }

    private void handleState(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        Cittadino citizen = pendingCitizen.get();
        String response = "{\"cittadino\":" + (citizen == null ? "null" : citizenJson(citizen)) + "}";
        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        try (exchange) {
            exchange.getResponseBody().write(body);
        }
    }

    private static String citizenJson(Cittadino citizen) {
        return "{"
                + "\"nome\":" + quote(citizen.getNome()) + ","
                + "\"cognome\":" + quote(citizen.getCognome()) + ","
                + "\"dataNascita\":" + quote(citizen.getDataNascita().toString()) + ","
                + "\"sesso\":" + quote(Character.toString(citizen.getSesso())) + ","
                + "\"comune\":" + quote(citizen.getComune()) + ","
                + "\"provincia\":" + quote(citizen.getProvincia()) + ","
                + "\"codiceComune\":" + quote(citizen.getCodiceComune()) + ","
                + "\"codiceFiscale\":" + quote(citizen.getCodiceFiscale())
                + "}";
    }

    private static String quote(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                + "\"";
    }
}
