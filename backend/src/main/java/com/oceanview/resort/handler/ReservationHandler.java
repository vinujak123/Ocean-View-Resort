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
        double totalRevenue = service.calculateTotalRevenue();

        // Revenue & count by room type
        Map<String, Double> revenueByRoom = new java.util.LinkedHashMap<>();
        Map<String, Integer> countByRoom = new java.util.LinkedHashMap<>();
        for (Reservation.RoomType rt : Reservation.RoomType.values()) {
            revenueByRoom.put(rt.name(), 0.0);
            countByRoom.put(rt.name(), 0);
        }
        // Revenue & count by board type
        Map<String, Double> revenueByBoard = new java.util.LinkedHashMap<>();
        Map<String, Integer> countByBoard = new java.util.LinkedHashMap<>();
        for (Reservation.BoardType bt : Reservation.BoardType.values()) {
            revenueByBoard.put(bt.name(), 0.0);
            countByBoard.put(bt.name(), 0);
        }

        long totalNights = 0;
        Map<String, Double> guestSpend = new java.util.LinkedHashMap<>();

        for (Reservation r : all) {
            // Room
            if (r.getRoomType() != null) {
                String rk = r.getRoomType().name();
                revenueByRoom.put(rk,
                        revenueByRoom.getOrDefault(rk, 0.0) + (r.getTotalBill() != null ? r.getTotalBill() : 0.0));
                countByRoom.put(rk, countByRoom.getOrDefault(rk, 0) + 1);
            }
            // Board
            if (r.getBoardType() != null) {
                String bk = r.getBoardType().name();
                revenueByBoard.put(bk,
                        revenueByBoard.getOrDefault(bk, 0.0) + (r.getTotalBill() != null ? r.getTotalBill() : 0.0));
                countByBoard.put(bk, countByBoard.getOrDefault(bk, 0) + 1);
            }
            // Nights
            if (r.getCheckInDate() != null && r.getCheckOutDate() != null) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
                totalNights += nights;
            }
            // Guest spend
            if (r.getGuestName() != null) {
                String g = r.getGuestName();
                guestSpend.put(g,
                        guestSpend.getOrDefault(g, 0.0) + (r.getTotalBill() != null ? r.getTotalBill() : 0.0));
            }
        }

        int count = all.size();
        double avgStay = count > 0 ? (double) totalNights / count : 0.0;
        double avgBill = count > 0 ? totalRevenue / count : 0.0;

        // Top guest
        String topGuest = "";
        double topSpend = 0.0;
        for (Map.Entry<String, Double> e : guestSpend.entrySet()) {
            if (e.getValue() > topSpend) {
                topSpend = e.getValue();
                topGuest = e.getKey();
            }
        }

        // Build response
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalBookings", count);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalNights", totalNights);
        stats.put("avgStayNights", Math.round(avgStay * 10.0) / 10.0);
        stats.put("avgBill", Math.round(avgBill));
        stats.put("topGuest", topGuest);
        stats.put("topGuestSpend", Math.round(topSpend));
        stats.put("revenueByRoom", revenueByRoom);
        stats.put("countByRoom", countByRoom);
        stats.put("revenueByBoard", revenueByBoard);
        stats.put("countByBoard", countByBoard);

        String json = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, json);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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
