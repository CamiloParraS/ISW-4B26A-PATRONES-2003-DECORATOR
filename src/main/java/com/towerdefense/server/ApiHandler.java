package com.towerdefense.server;

import com.towerdefense.engine.GameState;
import com.towerdefense.towers.BaseTower;
import com.towerdefense.towers.Tower;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiHandler implements HttpHandler {
    private static final Pattern TYPE_PATTERN = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern GX_PATTERN = Pattern.compile("\"gx\"\\s*:\\s*(-?\\d+)");
    private static final Pattern GY_PATTERN = Pattern.compile("\"gy\"\\s*:\\s*(-?\\d+)");
    private static final Pattern TOWER_ID_PATTERN =
            Pattern.compile("\"towerId\"\\s*:\\s*\"([^\"]+)\"");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(method) && "/api/start-wave".equals(path)) {
            GameState state = GameState.getInstance();
            state.enqueue(() -> {
                state.getWaveManager().startWave(state.nextWave());
                state.setRunning(true);
            });
            writeJson(exchange, 200, "{}");
            return;
        }

        if ("POST".equalsIgnoreCase(method) && "/api/place-tower".equals(path)) {
            handlePlaceTower(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(method) && "/api/sell".equals(path)) {
            handleSell(exchange);
            return;
        }

        writeJson(exchange, 404, "{\"error\":\"not_found\"}");
    }

    private void handlePlaceTower(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String type = captureGroup(TYPE_PATTERN, body);
        Integer gx = parseIntField(GX_PATTERN, body);
        Integer gy = parseIntField(GY_PATTERN, body);

        if (type == null || gx == null || gy == null) {
            writeJson(exchange, 400, "{\"error\":\"bad_input\"}");
            return;
        }

        AtomicInteger status = new AtomicInteger(200);
        AtomicReference<String> payload = new AtomicReference<>("{}");
        runInQueue(status, payload, () -> {
            GameState state = GameState.getInstance();

            if (!"dart".equalsIgnoreCase(type)) {
                status.set(400);
                payload.set("{\"error\":\"bad_input\"}");
                return;
            }

            if (!state.getMap().isWithinBounds(gx, gy)) {
                status.set(400);
                payload.set("{\"error\":\"bad_input\"}");
                return;
            }

            if (!state.getMap().canPlace(gx, gy) || state.getTowerAt(gx, gy) != null) {
                status.set(409);
                payload.set("{\"error\":\"occupied_or_unbuildable\"}");
                return;
            }

            if (!state.trySpendCoins(BaseTower.BUY_COST)) {
                status.set(402);
                payload.set("{\"error\":\"insufficient_coins\"}");
                return;
            }

            String id = UUID.randomUUID().toString();
            state.addTower(new BaseTower(id, gx, gy));
            state.getMap().setOccupied(gx, gy);
            payload.set("{\"towerId\":\"" + id + "\"}");
        });

        writeJson(exchange, status.get(), payload.get());
    }

    private void handleSell(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String towerId = captureGroup(TOWER_ID_PATTERN, body);

        if (towerId == null || towerId.isBlank()) {
            writeJson(exchange, 400, "{\"error\":\"bad_input\"}");
            return;
        }

        AtomicInteger status = new AtomicInteger(200);
        AtomicReference<String> payload = new AtomicReference<>("{}");
        runInQueue(status, payload, () -> {
            GameState state = GameState.getInstance();
            Tower tower = state.getTowerById(towerId);
            if (tower == null) {
                status.set(404);
                payload.set("{\"error\":\"not_found\"}");
                return;
            }

            int refund = tower.getSellValue();
            state.addCoins(refund);
            state.removeTower(towerId);
            state.getMap().clearOccupied(tower.getGridX(), tower.getGridY());
            payload.set("{\"refund\":" + refund + "}");
        });

        writeJson(exchange, status.get(), payload.get());
    }

    private static void runInQueue(AtomicInteger status, AtomicReference<String> payload,
            Runnable action) {
        GameState state = GameState.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        state.enqueue(() -> {
            try {
                action.run();
            } catch (RuntimeException ex) {
                status.set(500);
                payload.set("{\"error\":\"server_error\"}");
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                status.set(503);
                payload.set("{\"error\":\"queue_timeout\"}");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            status.set(503);
            payload.set("{\"error\":\"queue_interrupted\"}");
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String captureGroup(Pattern pattern, String body) {
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static Integer parseIntField(Pattern pattern, String body) {
        String value = captureGroup(pattern, body);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void writeJson(HttpExchange exchange, int statusCode, String payload)
            throws IOException {
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
