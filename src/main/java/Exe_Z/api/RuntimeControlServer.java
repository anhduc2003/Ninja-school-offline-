package Exe_Z.api;

import Exe_Z.server.Config;
import Exe_Z.server.GlobalService;
import Exe_Z.server.ServerManager;
import Exe_Z.server.WorldBossNotificationService;
import Exe_Z.bot.player.BotPlayerManager;
import Exe_Z.stall.StallManager;
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
    private static final String WORLD_BOSS_TEST_PATH = "/api/control/world-boss-test";
    private static final String SHINWA_SYNC_PATH = "/api/control/shinwa-sync";
    private static final String BOTS_LIST_PATH = "/api/control/bots";
    private static final String BOT_START_PATH = "/api/control/bot/start";
    private static final String BOT_STOP_PATH = "/api/control/bot/stop";
    private static final String BOT_ADD_PATH = "/api/control/bot/add";
    private static final String BOT_REMOVE_PATH = "/api/control/bot/remove";
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
            server.createContext(WORLD_BOSS_TEST_PATH, RuntimeControlServer::handleWorldBossTest);
            server.createContext(SHINWA_SYNC_PATH, RuntimeControlServer::handleShinwaSync);
            server.createContext(BOTS_LIST_PATH, RuntimeControlServer::handleBotsList);
            server.createContext(BOT_START_PATH, RuntimeControlServer::handleBotStart);
            server.createContext(BOT_STOP_PATH, RuntimeControlServer::handleBotStop);
            server.createContext(BOT_ADD_PATH, RuntimeControlServer::handleBotAdd);
            server.createContext(BOT_REMOVE_PATH, RuntimeControlServer::handleBotRemove);
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

    private static void handleWorldBossTest(HttpExchange exchange) throws IOException {
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
            long rawConfigId = ((Number) payload.getOrDefault("configId", 0L)).longValue();
            if (rawConfigId < 1 || rawConfigId > Integer.MAX_VALUE) {
                respond(exchange, 400, "{\"error\":\"Invalid configId\"}");
                return;
            }
            int onlinePlayers = WorldBossNotificationService.sendTest((int) rawConfigId);
            respond(exchange, 200, "{\"ok\":true,\"onlinePlayers\":" + onlinePlayers + "}");
        } catch (org.json.simple.parser.ParseException | ClassCastException exception) {
            respond(exchange, 400, "{\"error\":\"Invalid JSON payload\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            System.err.println("World boss test failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"World boss test failed\"}");
        } finally {
            exchange.close();
        }
    }

    private static void handleShinwaSync(HttpExchange exchange) throws IOException {
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
            long rawId = ((Number) payload.getOrDefault("uniqueId", 0L)).longValue();
            long rawPrice = ((Number) payload.getOrDefault("price", 0L)).longValue();
            long rawTime = ((Number) payload.getOrDefault("time", 0L)).longValue();
            long rawStatus = ((Number) payload.getOrDefault("status", -1L)).longValue();
            if (rawId < 1 || rawId > Integer.MAX_VALUE || rawPrice < 0 || rawPrice > Integer.MAX_VALUE || rawTime < 0 || rawTime > Integer.MAX_VALUE || rawStatus < 0 || rawStatus > 2) {
                respond(exchange, 400, "{\"error\":\"Invalid Shinwa listing fields\"}");
                return;
            }
            boolean found = StallManager.getInstance().applyAdminUpdate((int) rawId, (int) rawPrice, (int) rawTime, (byte) rawStatus);
            respond(exchange, 200, "{\"ok\":true,\"found\":" + found + "}");
        } catch (org.json.simple.parser.ParseException | ClassCastException exception) {
            respond(exchange, 400, "{\"error\":\"Invalid JSON payload\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            System.err.println("Shinwa sync failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"Shinwa sync failed\"}");
        } finally {
            exchange.close();
        }
    }

    private static void handleBotsList(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            if (!authorized(exchange)) {
                respond(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            java.util.List<BotPlayerManager.BotStatus> statuses = BotPlayerManager.getInstance().getBotStatuses();
            StringBuilder json = new StringBuilder("{\"ok\":true,\"bots\":[");
            for (int i = 0; i < statuses.size(); i++) {
                BotPlayerManager.BotStatus s = statuses.get(i);
                if (i > 0) json.append(",");
                json.append("{\"charId\":").append(s.charId)
                    .append(",\"name\":\"").append(jsonEscape(s.name)).append("\"")
                    .append(",\"active\":").append(s.active)
                    .append(",\"mapId\":").append(s.mapId)
                    .append(",\"hp\":").append(s.hp)
                    .append(",\"maxHp\":").append(s.maxHp)
                    .append(",\"mp\":").append(s.mp)
                    .append(",\"maxMp\":").append(s.maxMp)
                    .append("}");
            }
            json.append("]}");
            respond(exchange, 200, json.toString());
        } catch (Exception exception) {
            System.err.println("Bots list failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"Bots list failed\"}");
        } finally {
            exchange.close();
        }
    }

    private static void handleBotStart(HttpExchange exchange) throws IOException {
        handleBotAction(exchange, true);
    }

    private static void handleBotStop(HttpExchange exchange) throws IOException {
        handleBotAction(exchange, false);
    }

    private static void handleBotAction(HttpExchange exchange, boolean start) throws IOException {
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
            long rawCharId = ((Number) payload.getOrDefault("charId", 0L)).longValue();
            if (rawCharId < 1 || rawCharId > Integer.MAX_VALUE) {
                respond(exchange, 400, "{\"error\":\"Invalid charId\"}");
                return;
            }
            int charId = (int) rawCharId;
            boolean result;
            if (start) {
                result = BotPlayerManager.getInstance().startBot(charId);
            } else {
                BotPlayerManager.getInstance().stopBot(charId);
                result = true;
            }
            respond(exchange, 200, "{\"ok\":true,\"charId\":" + charId + ",\"action\":\"" + (start ? "start" : "stop") + "\",\"result\":" + result + "}");
        } catch (org.json.simple.parser.ParseException | ClassCastException exception) {
            respond(exchange, 400, "{\"error\":\"Invalid JSON payload\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            System.err.println("Bot " + (start ? "start" : "stop") + " failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"Bot action failed\"}");
        } finally {
            exchange.close();
        }
    }

    private static void handleBotAdd(HttpExchange exchange) throws IOException {
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
            long rawCharId = ((Number) payload.getOrDefault("charId", 0L)).longValue();
            if (rawCharId < 1 || rawCharId > Integer.MAX_VALUE) {
                respond(exchange, 400, "{\"error\":\"Invalid charId\"}");
                return;
            }
            int charId = (int) rawCharId;
            boolean started = BotPlayerManager.getInstance().addAndStartBot(charId);
            respond(exchange, 200, "{\"ok\":true,\"charId\":" + charId + ",\"started\":" + started + "}");
        } catch (org.json.simple.parser.ParseException | ClassCastException exception) {
            respond(exchange, 400, "{\"error\":\"Invalid JSON payload\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            System.err.println("Bot add failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"Bot add failed\"}");
        } finally {
            exchange.close();
        }
    }

    private static void handleBotRemove(HttpExchange exchange) throws IOException {
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
            long rawCharId = ((Number) payload.getOrDefault("charId", 0L)).longValue();
            if (rawCharId < 1 || rawCharId > Integer.MAX_VALUE) {
                respond(exchange, 400, "{\"error\":\"Invalid charId\"}");
                return;
            }
            int charId = (int) rawCharId;
            BotPlayerManager.getInstance().removeBotByCharId(charId);
            respond(exchange, 200, "{\"ok\":true,\"charId\":" + charId + "}");
        } catch (org.json.simple.parser.ParseException | ClassCastException exception) {
            respond(exchange, 400, "{\"error\":\"Invalid JSON payload\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            System.err.println("Bot remove failed: " + exception.getMessage());
            respond(exchange, 500, "{\"error\":\"Bot remove failed\"}");
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
