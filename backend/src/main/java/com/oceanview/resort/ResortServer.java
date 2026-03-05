package com.oceanview.resort;

import com.oceanview.resort.controller.*;
import com.oceanview.resort.repository.*;
import com.oceanview.resort.service.PricingService;
import com.oceanview.resort.service.ReservationService;
import com.oceanview.resort.util.DatabaseUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main server application for Ocean View Resort
 * Vanilla Java HTTP server without any framework dependencies
 */
public class ResortServer {

    private static final int PORT = Integer.parseInt(System.getProperty("server.port", "8081"));
    private static HttpServer server;

    public static void main(String[] args) {
        try {
            // Repositories
            ReservationRepository reservationRepository;
            UserRepository userRepository;

            // Pricing config always uses file-based storage
            FilePricingRepository pricingRepository = new FilePricingRepository("data/pricing.json");
            PricingService pricingService = new PricingService(pricingRepository);

            // Try MySQL Connection
            String persistence = System.getProperty("persistence", "auto");
            System.out.println("Persistence mode: " + persistence);

            if (!"file".equalsIgnoreCase(persistence) && DatabaseUtil.testConnection()) {
                System.out.println("Using MySQL persistence.");
                reservationRepository = new MySqlReservationRepository();
                userRepository = new MySqlUserRepository();

                // Migrate existing data if needed
                migrateData(reservationRepository, userRepository);
            } else {
                System.out.println("Using File-based persistence.");
                reservationRepository = new FileBasedReservationRepository();
                userRepository = new FileUserRepository();
            }

            ReservationService reservationService = new ReservationService(reservationRepository, pricingService);

            // Create HTTP server
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Register handlers (now Controllers)
            server.createContext("/api/reservations", new ReservationController(reservationService));
            server.createContext("/api/pricing", new PricingController(pricingService));
            server.createContext("/api/auth", new AuthController(userRepository));
            server.createContext("/api/users", new UserController(userRepository));
            server.createContext("/swagger-ui", new SwaggerController());
            server.createContext("/api-docs", new SwaggerController());

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop(0);
                System.out.println("Server stopped.");
            }));

            // Start server
            server.start();

            // Log absolute path for data for debugging
            System.out.println("Data Directory: " + java.nio.file.Paths.get("data").toAbsolutePath());

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

    private static void migrateData(ReservationRepository mysqlResRepo, UserRepository mysqlUserRepo) {
        try {
            // Check if users table is empty (ignore default users count if needed)
            if (mysqlUserRepo.findAll().size() <= 2) {
                System.out.println("Migrating users from file to MySQL...");
                FileUserRepository fileUserRepo = new FileUserRepository();
                fileUserRepo.findAll().forEach(mysqlUserRepo::save);
            }

            // Check if reservations table is empty
            if (mysqlResRepo.findAll().isEmpty()) {
                System.out.println("Migrating reservations from file to MySQL...");
                FileBasedReservationRepository fileResRepo = new FileBasedReservationRepository();
                fileResRepo.findAll().forEach(mysqlResRepo::save);
            }
        } catch (Exception e) {
            System.err.println("Migration warning: " + e.getMessage());
        }
    }
}
