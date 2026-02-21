package src;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String RESERVATIONS_FILE = "reservations.txt";
    private static final String USERS_FILE = "users.txt";

    public static List<Reservation> loadReservations() {
        List<Reservation> reservations = new ArrayList<>();
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists())
            return reservations;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    Guest guest = new Guest(parts[1], parts[2], parts[3]);
                    RoomType roomType = RoomType.fromString(parts[4]);
                    Reservation res = new Reservation(
                            parts[0],
                            guest,
                            roomType,
                            LocalDate.parse(parts[5]),
                            LocalDate.parse(parts[6]));
                    reservations.add(res);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading reservations: " + e.getMessage());
        }
        return reservations;
    }

    public static void saveReservations(List<Reservation> reservations) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            for (Reservation res : reservations) {
                writer.println(String.join(",",
                        res.getReservationNumber(),
                        res.getGuest().getName(),
                        res.getGuest().getAddress(),
                        res.getGuest().getContactNumber(),
                        res.getRoomType().getName(),
                        res.getCheckInDate().toString(),
                        res.getCheckOutDate().toString()));
            }
        } catch (IOException e) {
            System.err.println("Error saving reservations: " + e.getMessage());
        }
    }

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            // Create default admin user if file doesn't exist
            users.add(new User("admin", "admin123"));
            saveUsers(users);
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public static void saveUsers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.println(user.getUsername() + "," + user.getPassword());
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
