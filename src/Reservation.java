package src;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation implements Serializable {
    private String reservationNumber;
    private Guest guest;
    private RoomType roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public Reservation(String reservationNumber, Guest guest, RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationNumber = reservationNumber;
        this.guest = guest;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Getters and Setters
    public String getReservationNumber() { return reservationNumber; }
    public Guest getGuest() { return guest; }
    public RoomType getRoomType() { return roomType; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public double calculateTotalBill() {
        long nights = getNights();
        return (nights > 0 ? nights : 1) * roomType.getRate();
    }

    @Override
    public String toString() {
        return "Res# " + reservationNumber + " | " + guest.getName() + " | " + roomType.getName() + " | " + checkInDate + " to " + checkOutDate;
    }
}
