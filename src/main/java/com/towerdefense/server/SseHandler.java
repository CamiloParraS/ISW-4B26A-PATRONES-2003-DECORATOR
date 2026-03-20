package com.towerdefense.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SseHandler implements HttpHandler {
    private static final CopyOnWriteArrayList<OutputStream> CLIENTS = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    static {
        HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            byte[] heartbeat = ": heartbeat\n\n".getBytes(StandardCharsets.UTF_8);
            for (OutputStream client : CLIENTS) {
                try {
                    client.write(heartbeat);
                    client.flush();
                } catch (IOException e) {
                    CLIENTS.remove(client);
                    try {
                        client.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);

        OutputStream out = exchange.getResponseBody();
        CLIENTS.add(out);

        try {
            while (true) {
                Thread.sleep(60_000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            CLIENTS.remove(out);
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void broadcast(String jsonPayload) {
        byte[] data = ("data: " + jsonPayload + "\n\n").getBytes(StandardCharsets.UTF_8);
        for (OutputStream client : CLIENTS) {
            try {
                client.write(data);
                client.flush();
            } catch (IOException e) {
                CLIENTS.remove(client);
                try {
                    client.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
