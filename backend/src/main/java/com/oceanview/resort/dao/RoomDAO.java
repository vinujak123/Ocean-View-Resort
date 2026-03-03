package com.oceanview.resort.dao;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.Reservation;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Rooms.
 */
public class RoomDAO {

    /**
     * Mock implementation of getting available rooms to match the slide's
     * requirements.
     */
    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(1L, "101", Reservation.RoomType.STANDARD, true));
        rooms.add(new Room(2L, "102", Reservation.RoomType.DELUXE, true));
        rooms.add(new Room(3L, "103", Reservation.RoomType.SUITE, true));
        return rooms;
    }
}
