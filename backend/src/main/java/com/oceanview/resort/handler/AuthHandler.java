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
            handleLogin(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject loginData = gson.fromJson(isr, JsonObject.class);

        String username = loginData.get("username").getAsString();
        String password = loginData.get("password").getAsString();

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            JsonObject response = new JsonObject();
            response.addProperty("username", user.getUsername());
            response.addProperty("role", user.getRole());
            response.addProperty("success", true);

            sendResponse(exchange, 200, response.toString());
        } else {
            JsonObject response = new JsonObject();
            response.addProperty("success", false);
            response.addProperty("message", "Invalid credentials");
            sendResponse(exchange, 401, response.toString());
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
