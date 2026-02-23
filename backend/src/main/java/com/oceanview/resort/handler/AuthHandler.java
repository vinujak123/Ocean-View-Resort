package com.oceanview.resort.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.resort.model.User;
import com.oceanview.resort.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class AuthHandler implements HttpHandler {
    private final UserRepository userRepository;
    private final Gson gson = new Gson();

    public AuthHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Add CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            System.out.println("Handling login request...");
            handleLogin(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            // Read body as string
            StringBuilder bodyBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    bodyBuilder.append(line);
                }
            }
            String body = bodyBuilder.toString();
            System.out.println("Request body: " + body);

            if (body.isEmpty()) {
                System.out.println("Empty request body!");
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Empty request\"}");
                return;
            }

            JsonObject loginData = gson.fromJson(body, JsonObject.class);

            if (loginData == null) {
                System.out.println("Login data is null!");
                sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid JSON\"}");
                return;
            }

            String username = loginData.get("username").getAsString();
            String password = loginData.get("password").getAsString();
            System.out.println("Login attempt for: " + username);

            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
                System.out.println("Login successful for " + username);
                User user = userOpt.get();
                JsonObject response = new JsonObject();
                response.addProperty("username", user.getUsername());
                response.addProperty("role", user.getRole());
                response.addProperty("success", true);

                sendResponse(exchange, 200, response.toString());
            } else {
                System.out.println("Login failed for " + username);
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Invalid credentials");
                sendResponse(exchange, 401, response.toString());
            }
        } catch (Exception e) {
            System.err.println("Error in handleLogin: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Internal error\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
