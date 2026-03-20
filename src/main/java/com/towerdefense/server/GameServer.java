package com.towerdefense.server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GameServer {
    private GameServer() {}

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ExecutorService executor = Executors.newFixedThreadPool(8);

        server.createContext("/", new StaticHandler());
        server.createContext("/api", new ApiHandler());
        server.createContext("/events", new SseHandler());

        server.setExecutor(executor);
        server.start();

        System.out.println("Game running at http://localhost:" + port);
    }
}
