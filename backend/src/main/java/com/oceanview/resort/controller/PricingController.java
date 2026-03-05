package com.oceanview.resort.controller;

import com.google.gson.JsonSyntaxException;
import com.oceanview.resort.model.PricingConfig;
import com.oceanview.resort.service.PricingService;
import com.oceanview.resort.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PricingController implements HttpHandler {

    private final PricingService service;

    public PricingController(PricingService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/pricing".equals(path)) {
                if ("GET".equals(method)) {
                    handleGet(exchange);
                } else if ("PUT".equals(method)) {
                    handleUpdate(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
                }
            } else {
                sendResponse(exchange, 404, "{\"message\":\"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        PricingConfig config = service.getPricing();
        String json = JsonUtil.toJson(config);
        sendResponse(exchange, 200, json);
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            PricingConfig newConfig = JsonUtil.fromJson(requestBody, PricingConfig.class);
            service.updatePricing(newConfig);
            String json = JsonUtil.toJson(service.getPricing());
            sendResponse(exchange, 200, json);
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "{\"message\":\"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Role");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
