package src;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ReservationSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private final ReservationManager resManager;
    private final List<User> staffUsers;
    private User currentUser;

    public ReservationSystem() {
        this.resManager = new ReservationManager();
        this.staffUsers = FileHandler.loadUsers();
    }

    public void start() {
        showWelcomeBanner();
        if (login()) {
            mainMenu();
        } else {
            System.out.println("Too many failed login attempts. Exiting...");
        }
    }

    private void showWelcomeBanner() {
        System.out.println("=================================================");
        System.out.println("         OCEAN VIEW RESORT - GALLE               ");
        System.out.println("      Digital Reservation & Billing System       ");
        System.out.println("=================================================");
    }

    private boolean login() {
        int attempts = 3;
        while (attempts > 0) {
            System.out.println("\n--- STAFF LOGIN ---");
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            for (User user : staffUsers) {
                if (user.getUsername().equals(username) && user.authenticate(password)) {
                    currentUser = user;
                    System.out.println("\nLogin Successful! Welcome, " + username + ".");
                    return true;
                }
            }
            attempts--;
            System.out.println("Invalid credentials. Attempts remaining: " + attempts);
        }
        return false;
    }

    private void mainMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println("-------------------------------------------------");
            System.out.println("                MAIN MENU (" + currentUser.getUsername() + ")");
            System.out.println("-------------------------------------------------");
            System.out.println("1. Add New Reservation");
            System.out.println("2. Display Reservation Details");
            System.out.println("3. Calculate and Print Bill");
            System.out.println("4. View All Reservations");
            System.out.println("5. System Help Section");
            System.out.println("6. Exit System");
            System.out.print("\nSelect an option (1-6): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    addNewReservation();
                    break;
                case "2":
                    displayReservationDetails();
                    break;
                case "3":
                    calculateAndPrintBill();
                    break;
                case "4":
                    viewAllReservations();
                    break;
                case "5":
                    showHelp();
                    break;
                case "6":
                    System.out.println("Thank you for using Ocean View Resort System. Goodbye!");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addNewReservation() {
        System.out.println("\n--- ADD NEW RESERVATION ---");

        System.out.print("Enter Reservation Number (e.g., OV-101): ");
        String resNum = scanner.nextLine();

        System.out.print("Guest Name: ");
        String name = scanner.nextLine();

        System.out.print("Guest Address: ");
        String address = scanner.nextLine();

        System.out.print("Contact Number: ");
        String contact = scanner.nextLine();

        System.out.println("Select Room Type:");
        for (RoomType rt : RoomType.values()) {
            System.out.println("- " + rt.getName() + " (LKR " + rt.getRate() + ")");
        }
        System.out.print("Choice: ");
        String roomTypeStr = scanner.nextLine();
        RoomType rt = RoomType.fromString(roomTypeStr);
        while (rt == null) {
            System.out.print("Invalid type. Please enter (Standard/Deluxe/Suite): ");
            rt = RoomType.fromString(scanner.nextLine());
        }

        LocalDate checkIn = null;
        while (checkIn == null) {
            System.out.print("Check-in Date (YYYY-MM-DD): ");
            try {
                checkIn = LocalDate.parse(scanner.nextLine());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Use YYYY-MM-DD.");
            }
        }

        LocalDate checkOut = null;
        while (checkOut == null) {
            System.out.print("Check-out Date (YYYY-MM-DD): ");
            try {
                checkOut = LocalDate.parse(scanner.nextLine());
                if (!checkOut.isAfter(checkIn)) {
                    System.out.println("Check-out must be after check-in.");
                    checkOut = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Use YYYY-MM-DD.");
            }
        }

        Guest guest = new Guest(name, address, contact);
        Reservation res = new Reservation(resNum, guest, rt, checkIn, checkOut);
        resManager.addReservation(res);

        System.out.println("\nSUCCESS: Reservation " + resNum + " added for " + name + ".");
    }

    private void displayReservationDetails() {
        System.out.print("\nEnter Reservation Number to retrieve: ");
        String resNum = scanner.nextLine();

        resManager.findReservation(resNum).ifPresentOrElse(
                res -> {
                    System.out.println("\n-------------------------------------------------");
                    System.out.println("           RESERVATION DETAILS                   ");
                    System.out.println("-------------------------------------------------");
                    System.out.println("Reservation ID  : " + res.getReservationNumber());
                    System.out.println("Guest Name      : " + res.getGuest().getName());
                    System.out.println("Contact Number  : " + res.getGuest().getContactNumber());
                    System.out.println("Address         : " + res.getGuest().getAddress());
                    System.out.println("Room Type       : " + res.getRoomType().getName());
                    System.out.println("Check-in Date   : " + res.getCheckInDate());
                    System.out.println("Check-out Date  : " + res.getCheckOutDate());
                    System.out.println("Total Nights    : " + res.getNights());
                    System.out.println("-------------------------------------------------");
                },
                () -> System.out.println("Error: Reservation not found."));
    }

    private void calculateAndPrintBill() {
        System.out.print("\nEnter Reservation Number for billing: ");
        String resNum = scanner.nextLine();

        resManager.findReservation(resNum).ifPresentOrElse(
                res -> {
                    double total = res.calculateTotalBill();
                    System.out.println("\n=================================================");
                    System.out.println("             OCEAN VIEW RESORT - BILL            ");
                    System.out.println("=================================================");
                    System.out.println("Bill For: " + res.getGuest().getName());
                    System.out.println("Res#    : " + res.getReservationNumber());
                    System.out.println("Room    : " + res.getRoomType().getName());
                    System.out.println("Rate    : LKR " + res.getRoomType().getRate() + " / night");
                    System.out.println("Nights  : " + res.getNights());
                    System.out.println("-------------------------------------------------");
                    System.out.printf("TOTAL AMOUNT: LKR %,.2f\n", total);
                    System.out.println("=================================================");
                    System.out.println("        Thank you for staying with us!           ");
                },
                () -> System.out.println("Error: Reservation not found."));
    }

    private void viewAllReservations() {
        List<Reservation> all = resManager.getAllReservations();
        if (all.isEmpty()) {
            System.out.println("\nNo reservations found.");
            return;
        }

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(String.format("%-10s | %-20s | %-10s | %-12s | %-12s", "ID", "Guest", "Room", "In", "Out"));
        System.out.println("--------------------------------------------------------------------------------");
        for (Reservation res : all) {
            System.out.println(String.format("%-10s | %-20s | %-10s | %-12s | %-12s",
                    res.getReservationNumber(),
                    res.getGuest().getName(),
                    res.getRoomType().getName(),
                    res.getCheckInDate(),
                    res.getCheckOutDate()));
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void showHelp() {
        System.out.println("\n--- SYSTEM HELP SECTION ---");
        System.out.println("1. Login: Use authorized staff credentials to access the system.");
        System.out.println("2. Add Reservation: Collect all guest details and select the appropriate room type.");
        System.out.println("3. Dates: Always use the format YYYY-MM-DD for consistency.");
        System.out.println("4. Billing: Ensure the check-out date is correctly entered to calculate total nights.");
        System.out.println("5. Security: Close the application or logout after your shift.");
        System.out.println("\nContact IT support for password resets.");
    }

    public static void main(String[] args) {
        ReservationSystem system = new ReservationSystem();
        system.start();
    }
}
