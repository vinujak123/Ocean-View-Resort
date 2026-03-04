package com.oceanview.resort.dao;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.Reservation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DAOManualTest {

    @Test
    public void runManualVerification() {
        System.out.println("==================================================");
        System.out.println("       OCEAN VIEW RESORT - DAO MANUAL TEST        ");
        System.out.println("==================================================");

        // 1. Test RoomDAO
        System.out.println("\n[TEST 1] Testing RoomDAO.getAvailableRooms()...");
        RoomDAO roomDAO = new RoomDAO();
        List<Room> rooms = roomDAO.getAvailableRooms();

        System.out.println("Result: Success");
        System.out.println("Available Rooms Found: " + rooms.size());
        assertFalse(rooms.isEmpty(), "Available rooms should not be empty");
        for (Room r : rooms) {
            System.out.println("  - Room #" + r.getRoomNumber() + " [" + r.getRoomType() + "] Status: "
                    + (r.isAvailable() ? "Available" : "Occupied"));
        }

        // 2. Test ReservationDAO
        System.out.println("\n[TEST 2] Testing ReservationDAO.createReservation()...");
        ReservationDAO resDAO = new ReservationDAO();
        Reservation res = new Reservation();
        String refId = "M-" + System.currentTimeMillis();
        res.setReferenceId(refId);
        res.setGuestName("Manual Test User");
        res.setAddress("456 Beach Road, Unawatuna");
        res.setPhone("0779876543");
        res.setRoomType(Reservation.RoomType.DELUXE);
        res.setBoardType(Reservation.BoardType.BB);
        res.setCheckInDate(LocalDate.now());
        res.setCheckOutDate(LocalDate.now().plusDays(2));
        res.setTotalBill(45000.0);

        boolean success = resDAO.createReservation(res);
        assertTrue(success, "Reservation creation should succeed");
        System.out.println("Result: Success - Reservation " + refId + " created for Manual Test User");

        System.out.println("\n==================================================");
        System.out.println("           END OF DAO MANUAL TEST                 ");
        System.out.println("==================================================");
    }
}
