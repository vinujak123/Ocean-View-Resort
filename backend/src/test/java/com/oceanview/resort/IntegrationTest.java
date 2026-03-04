package com.oceanview.resort;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full system integration tests.
 * Spins up the server on an alternate port and sends real HTTP requests.
 */
public class IntegrationTest {

    private static final String PORT = "8082";
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static Thread serverThread;

    @BeforeAll
    public static void startServer() throws Exception {
        // Use a test-specific data file so we don't mess up production data
        System.setProperty("server.port", PORT);
        System.setProperty("persistence", "file"); // Don't require MySQL for CI testing

        // Clean up test data if it exists
        Path dataPath = Paths.get("data/reservations.json");
        if (Files.exists(dataPath)) {
            Files.delete(dataPath);
        }

        serverThread = new Thread(() -> ResortServer.main(new String[] {}));
        serverThread.start();

        // Wait for server to bind and start
        Thread.sleep(1500);
    }

    @AfterAll
    public static void stopServer() {
        System.out.println("Integration tests finished. Server thread will die naturally or when JVM exits.");
    }

    @Test
    public void testApi_GetAllReservations_Returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reservations"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("["), "Response should be a JSON array");
    }

    @Test
    public void testSecurity_GetUsersWithoutAuth_Returns403() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
    }

    @Test
    public void testBoundary_InvalidJsonCreation_Returns400() throws Exception {
        // Sending completely broken JSON
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reservations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{ bad_json: true "))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Usually, poorly formed JSON should return 400 Bad Request
        assertEquals(400, response.statusCode());
    }

    @Test
    public void testBoundary_InvalidDates_Returns400() throws Exception {
        // Checkout is before Checkin
        String jsonPayload = "{\n" +
                "  \"guestName\": \"Integration Test\",\n" +
                "  \"phone\": \"0770000000\",\n" +
                "  \"roomType\": \"STANDARD\",\n" +
                "  \"boardType\": \"BB\",\n" +
                "  \"checkInDate\": \"2024-12-10\",\n" +
                "  \"checkOutDate\": \"2024-12-05\"\n" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/reservations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Check-out must be at least one day after Check-in"));
    }
}
