package com.oceanview.resort.model;

import java.time.LocalDate;

/**
 * Reservation model - Plain Java object (POJO)
 */
public class Reservation {
    private Long id;
    private String referenceId;
    private String guestName;
    private String address;
    private String phone;
    private RoomType roomType;
    private BoardType boardType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Double totalBill;

    // Enums
    public enum RoomType {
        STANDARD(15000.0), DELUXE(25000.0), SUITE(45000.0);

        public final Double rate;

        RoomType(Double rate) {
            this.rate = rate;
        }
    }

    public enum BoardType {
        BB("Bed & Breakfast", 0.0),
        HB("Half Board", 5000.0),
        FB("Full Board", 10000.0);

        public final String name;
        public final Double rate;

        BoardType(String name, Double rate) {
            this.name = name;
            this.rate = rate;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public BoardType getBoardType() {
        return boardType;
    }

    public void setBoardType(BoardType boardType) {
        this.boardType = boardType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Double getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(Double totalBill) {
        this.totalBill = totalBill;
    }
}
