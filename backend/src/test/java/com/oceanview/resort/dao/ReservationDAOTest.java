package com.oceanview.resort.dao;

import com.oceanview.resort.model.Reservation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationDAOTest {

    @Test
    public void testCreateReservation() {
        ReservationDAO dao = new ReservationDAO();
        Reservation res = new Reservation();

        // Setup reservation details as shown in Slide 17 pattern
        res.setReferenceId("203");
        res.setGuestName("Test Guest");
        res.setAddress("123 Galle Road, Galle");
        res.setPhone("0771234567");
        res.setRoomType(Reservation.RoomType.STANDARD);
        res.setBoardType(Reservation.BoardType.BB);
        res.setCheckInDate(LocalDate.now().plusDays(1));
        res.setCheckOutDate(LocalDate.now().plusDays(3));
        res.setTotalBill(30000.0);

        boolean result = dao.createReservation(res);
        assertTrue(result, "Reservation creation should succeed");
    }
}
