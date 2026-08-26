package Exe_Z.api;

import Exe_Z.server.Config;
import Exe_Z.server.GlobalService;
import Exe_Z.server.ServerManager;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Local-only runtime bridge used by the admin panel for server-wide notices.
 * The listener is disabled unless server.control.token is configured.
 */
public final class RuntimeControlServer {

    private static final String HOST = "127.0.0.1";
    private static final int MAX_BODY_BYTES = 16 * 1024;
    private static final String BROADCAST_PATH = "/api/control/broadcast";
    private static HttpServer server;

    private RuntimeControlServer() {
    }

    public static synchronized void start() {
        Config config = Config.getInstance();
        String token = config.getControlToken();
        if (token == null || token.isBlank()) {
            System.out.println("Runtime control disabled: server.control.token is not configured.");
            return;
        }
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(HOST, config.getControlPort()), 0);
            server.createContext("/api/control/health", RuntimeControlServer::handleHealth);
            server.createContext(BROADCAST_PATH, RuntimeControlServer::handleBroadcast);
            server.setExecutor(Executors.newFixedThreadPool(2, runnable -> {
                Thread thread = new Thread(runnable, "runtime-control");
                thread.setDaemon(true);
                return thread;
            }));
            server.start();
            System.out.println("Runtime control listening on http://" + HOST + ":" + config.getControlPort());
        } catch (IOException exception) {
            server = null;
            System.err.println("Runtime control failed to start: " + exception.getMessage());
        }
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            if (!authorized(exchange)) {
                respond(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            respond(exchange, 200, "{\"ok\":true,\"onlinePlayers\":" + ServerManager.getNumberOnline() + "}");
        } finally {
            exchange.close();
        }
    }

    private static void handleBroadcast(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            if (!authorized(exchange)) {
                respond(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            byte[] body = readBody(exchange.getRequestBody());
            JSONObject payload = (JSONObject) new JSONParser().parse(new String(body, StandardCharsets.UTF_8));
            String sender = normalized(payload.get("sender"), 40);
            String message = normalized(payload.get("message"), 500);
            if (sender.isBlank() || message.isBlank()) {
                respond(exchange, 400, "{\"error\":\"sender and message are required\"}");
                return;
            }
            GlobalService.getInstance().chat(sender, message);
            int onlinePlayers = ServerManager.getNumberOnline();
            respond(exchange, 200, "{\"ok\":true,\"onlinePlayers\":" + onlinePlayers + "}");
        } catch (org.json.simple.parser.ParseException | ClassCastException exception) {
            respond(exchange, 400, "{\"error\":\"Invalid JSON payload\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            System.err.println("Runtime control broadcast failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"Broadcast failed\"}");
        } finally {
            exchange.close();
        }
    }

    private static boolean authorized(HttpExchange exchange) {
        Headers headers = exchange.getRequestHeaders();
        String actual = headers.getFirst("Authorization");
        String expected = "Bearer " + Config.getInstance().getControlToken();
        return expected.equals(actual);
    }

    private static byte[] readBody(InputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        int total = 0;
        int read;
        try (input) {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_BODY_BYTES) {
                    throw new IllegalArgumentException("Request body too large");
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static String normalized(Object value, int maxLength) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        if (text.length() > maxLength) {
            throw new IllegalArgumentException("Field exceeds " + maxLength + " characters");
        }
        return text;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
