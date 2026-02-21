package com.oceanview.resort.handler;

import com.google.gson.JsonSyntaxException;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.service.ReservationService;
import com.oceanview.resort.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP handler for Reservation API endpoints
 */
public class ReservationHandler implements HttpHandler {

    private final ReservationService service;

    public ReservationHandler(ReservationService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Add CORS headers
        addCorsHeaders(exchange);

        // Handle preflight OPTIONS request
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.equals("/api/reservations")) {
                if ("GET".equals(method)) {
                    handleGetAll(exchange);
                } else if ("POST".equals(method)) {
                    handleCreate(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
                }
            } else if (path.equals("/api/reservations/stats")) {
                if ("GET".equals(method)) {
                    handleGetStats(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
                }
            } else if (path.startsWith("/api/reservations/")) {
                String refId = path.substring("/api/reservations/".length());
                if ("GET".equals(method)) {
                    handleGetByRefId(exchange, refId);
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

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Reservation> reservations = service.getAll();
        String json = JsonUtil.toJson(reservations);
        sendResponse(exchange, 200, json);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Reservation reservation = JsonUtil.fromJson(requestBody, Reservation.class);

            Reservation created = service.create(reservation);
            String json = JsonUtil.toJson(created);
            sendResponse(exchange, 200, json);
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "{\"message\":\"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetByRefId(HttpExchange exchange, String refId) throws IOException {
        Reservation reservation = service.getByRefId(refId);
        if (reservation != null) {
            String json = JsonUtil.toJson(reservation);
            sendResponse(exchange, 200, json);
        } else {
            sendResponse(exchange, 404, "{\"message\":\"Reservation not found\"}");
        }
    }

    private void handleGetStats(HttpExchange exchange) throws IOException {
        List<Reservation> all = service.getAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", all.size());
        stats.put("totalRevenue", service.calculateTotalRevenue());
        stats.put("occupancyRate", "85%"); // Mocked for demonstration

        String json = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, json);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
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
