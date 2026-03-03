package com.oceanview.resort.dao;

import com.oceanview.resort.model.Room;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoomDAOTest {

    @Test
    public void testGetAvailableRooms() {
        RoomDAO dao = new RoomDAO();
        List<Room> availableRooms = dao.getAvailableRooms();

        assertNotNull(availableRooms, "Available rooms list should not be null");
        assertFalse(availableRooms.isEmpty(), "Available rooms list should not be empty");

        System.out.println("Found " + availableRooms.size() + " available rooms.");
        for (Room room : availableRooms) {
            System.out.println("Room: " + room.getRoomNumber() + " (" + room.getRoomType() + ")");
        }
    }
}
