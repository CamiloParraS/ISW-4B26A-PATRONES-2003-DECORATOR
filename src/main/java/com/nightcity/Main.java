package com.nightcity;

import com.nightcity.web.HttpHandlers;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

/**
 * Entry point.
 *
 * Starts a com.sun.net.httpserver on port 8080 and wires two contexts:
 *   GET  /        → renders the game UI
 *   POST /action  → processes player commands, redirects back to /
 *
 * The server uses the built-in default executor (one handler thread).
 * All game state lives in a single {@link GameState} instance.
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        GameState    state    = new GameState();
        HttpHandlers handlers = new HttpHandlers(state);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/",       exchange -> {
            try { handlers.handleRoot(exchange);   }
            catch (Exception e) { e.printStackTrace(); }
        });

        server.createContext("/action", exchange -> {
            try { handlers.handleAction(exchange); }
            catch (Exception e) {
                e.printStackTrace();
                // Best-effort redirect so the browser doesn't hang
                try {
                    exchange.getResponseHeaders().set("Location", "/");
                    exchange.sendResponseHeaders(302, -1);
                    exchange.getResponseBody().close();
                } catch (Exception ignored) {}
            }
        });

        server.setExecutor(null); // uses default single-thread executor
        server.start();

        System.out.println("+================================================+");
        System.out.println("|   NIGHT CITY TOWER DEFENSE  //  PORT: " + PORT + "      |");
        System.out.println("|   http://localhost:" + PORT + "                        |");
        System.out.println("+================================================+");
        System.out.println("  1. Deploy turrets at slots 20 / 40 / 60 / 80");
        System.out.println("  2. Install modules (decorators) to upgrade them");
        System.out.println("  3. START NEXT WAVE, then use STEP / RUN buttons");
        System.out.println();

        // Keep the main thread alive; the HttpServer runs on its own thread.
        Thread.currentThread().join();
    }
}
