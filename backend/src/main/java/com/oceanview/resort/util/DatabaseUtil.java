package com.oceanview.resort.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;

public class DatabaseUtil {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String URL = "jdbc:mysql://localhost:3306/oceanview?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try {
            // First try with the database name
            try (Connection conn = getConnection()) {
                initializeSchema(conn);
                return true;
            } catch (SQLException e) {
                // If DB doesn't exist, try connecting to MySQL base URL to create it
                try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD)) {
                    initializeSchema(conn);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    private static void initializeSchema(Connection conn) {
        System.out.println("Initializing database schema if needed...");
        try (InputStream is = DatabaseUtil.class.getResourceAsStream("/schema.sql")) {
            if (is == null) {
                System.err.println("schema.sql not found in resources.");
                return;
            }
            String schemaSql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] commands = schemaSql.split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String command : commands) {
                    if (!command.trim().isEmpty()) {
                        stmt.execute(command.trim());
                    }
                }
            }
            System.out.println("Database initialization completed successfully.");
        } catch (Exception e) {
            System.err.println("Warning: Schema initialization error: " + e.getMessage());
            // It might already be initialized if they did it manually
        }
    }
}
