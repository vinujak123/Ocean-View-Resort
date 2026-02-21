package com.oceanview.resort;

import com.oceanview.resort.handler.ReservationHandler;
import com.oceanview.resort.handler.AuthHandler;
import com.oceanview.resort.handler.UserHandler;
import com.oceanview.resort.handler.SwaggerHandler;
import com.oceanview.resort.repository.FileBasedReservationRepository;
import com.oceanview.resort.repository.UserRepository;
import com.oceanview.resort.service.ReservationService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main server application for Ocean View Resort
 * Vanilla Java HTTP server without any framework dependencies
 */
public class ResortServer {

    private static final int PORT = 8081;
    private static HttpServer server;

    public static void main(String[] args) {
        try {
            // Initialize repositories and services
            FileBasedReservationRepository repository = new FileBasedReservationRepository();
            UserRepository userRepository = new UserRepository();
            ReservationService service = new ReservationService(repository);

            // Create HTTP server
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Register handlers
            server.createContext("/api/reservations", new ReservationHandler(service));
            server.createContext("/api/auth", new AuthHandler(userRepository));
            server.createContext("/api/users", new UserHandler(userRepository));
            server.createContext("/swagger-ui", new SwaggerHandler());
            server.createContext("/api-docs", new SwaggerHandler());

            // Set executor for handling requests
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop(0);
                System.out.println("Server stopped.");
            }));

            // Start server
            server.start();
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║        Ocean View Resort - Reservation System             ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Server started successfully!");
            System.out.println("Port: " + PORT);
            System.out.println();
            System.out.println("API Endpoints:");
            System.out.println("  - GET    http://localhost:" + PORT + "/api/reservations");
            System.out.println("  - POST   http://localhost:" + PORT + "/api/reservations");
            System.out.println("  - GET    http://localhost:" + PORT + "/api/reservations/{refId}");
            System.out.println("  - GET    http://localhost:" + PORT + "/api/reservations/stats");
            System.out.println();
            System.out.println("API Documentation:");
            System.out.println("  - Swagger UI: http://localhost:" + PORT + "/swagger-ui");
            System.out.println();
            System.out.println("Press Ctrl+C to stop the server.");

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
