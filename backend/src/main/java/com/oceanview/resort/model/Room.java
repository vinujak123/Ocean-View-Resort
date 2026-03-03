package com.oceanview.resort.model;

/**
 * Room model representing a hotel room.
 */
public class Room {
    private Long id;
    private String roomNumber;
    private Reservation.RoomType roomType;
    private boolean available;

    public Room() {
    }

    public Room(Long id, String roomNumber, Reservation.RoomType roomType, boolean available) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Reservation.RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(Reservation.RoomType roomType) {
        this.roomType = roomType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
