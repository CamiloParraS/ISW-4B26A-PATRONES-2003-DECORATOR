package com.towerdefense.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class StaticHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Default to index.html if the root is requested
        if (path.equals("/")) {
            path = "/index.html";
        }

        // Remove the leading slash to look up in resources
        String resourcePath = path.startsWith("/") ? path.substring(1) : path;

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                String notFound = "404 (Not Found): " + resourcePath;
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(notFound.getBytes());
                }
                return;
            }

            byte[] body = input.readAllBytes();

            // SET THE CORRECT MIME TYPE
            String contentType = "text/plain";
            if (path.endsWith(".html"))
                contentType = "text/html";
            else if (path.endsWith(".css"))
                contentType = "text/css";
            else if (path.endsWith(".js"))
                contentType = "application/javascript";

            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }
}
