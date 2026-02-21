package com.oceanview.resort.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HTTP handler for serving Swagger UI and OpenAPI specification
 */
public class SwaggerHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Add CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (path.equals("/swagger-ui") || path.equals("/swagger-ui/")) {
            serveSwaggerUI(exchange);
        } else if (path.equals("/api-docs/openapi.json")) {
            serveOpenApiSpec(exchange);
        } else if (path.startsWith("/swagger-ui/")) {
            serveSwaggerResource(exchange, path);
        } else {
            sendResponse(exchange, 404, "Not found");
        }
    }

    private void serveSwaggerUI(HttpExchange exchange) throws IOException {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ocean View Resort API</title>
                    <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui.css">
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-bundle.js"></script>
                    <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-standalone-preset.js"></script>
                    <script>
                        window.onload = function() {
                            SwaggerUIBundle({
                                url: "/api-docs/openapi.json",
                                dom_id: '#swagger-ui',
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIStandalonePreset
                                ],
                                layout: "StandaloneLayout"
                            });
                        };
                    </script>
                </body>
                </html>
                                """;

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void serveOpenApiSpec(HttpExchange exchange) throws IOException {
        Path specPath = Paths.get("backend/src/main/resources/openapi.json");
        if (Files.exists(specPath)) {
            String json = Files.readString(specPath);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            sendResponse(exchange, 404, "OpenAPI specification not found");
        }
    }

    private void serveSwaggerResource(HttpExchange exchange, String path) throws IOException {
        // For additional resources if needed (CSS, JS from WebJars)
        sendResponse(exchange, 404, "Resource not found");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
