package com.nightcity.web;

import com.nightcity.GameState;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contains the two HTTP handlers wired to the HttpServer in Main.
 *
 *   GET  /        → renders the full game UI as HTML
 *   POST /action  → mutates GameState, then redirects back to GET /
 *
 * All state mutation is single-threaded via the HttpServer's default
 * executor, which is safe for this in-memory, single-user game.
 */
public class HttpHandlers {

    private final GameState state;

    public HttpHandlers(GameState state) {
        this.state = state;
    }

    // ── GET / ─────────────────────────────────────────────────────────

    public void handleRoot(HttpExchange exchange) throws IOException {
        String html  = HtmlBuilder.build(state);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    // ── POST /action ──────────────────────────────────────────────────

    public void handleAction(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> params = parseFormBody(exchange.getRequestBody());
            String cmd = params.getOrDefault("cmd", "");

            switch (cmd) {
                case "place" ->
                    state.placeTower(intParam(params, "pos", 20));
                case "upgrade" ->
                    state.upgradeTower(intParam(params, "idx", 0),
                                       params.getOrDefault("dec", ""));
                case "wave"  -> state.startWave();
                case "tick"  -> state.tick();
                case "run5"  -> { for (int i = 0; i < 5;  i++) state.tick(); }
                case "run20" -> { for (int i = 0; i < 20; i++) state.tick(); }
                default      -> state.addLog("[ERR] Unknown command: " + cmd);
            }
        }
        redirect(exchange);
    }

    // ── Utilities ─────────────────────────────────────────────────────

    private static Map<String, String> parseFormBody(InputStream body) throws IOException {
        String raw = new String(body.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new LinkedHashMap<>();
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static int intParam(Map<String, String> params, String key, int fallback) {
        try { return Integer.parseInt(params.getOrDefault(key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static void redirect(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }
}
