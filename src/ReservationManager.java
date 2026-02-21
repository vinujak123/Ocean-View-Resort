package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationManager {
    private List<Reservation> reservations;

    public ReservationManager() {
        this.reservations = FileHandler.loadReservations();
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        FileHandler.saveReservations(reservations);
    }

    public Optional<Reservation> findReservation(String reservationNumber) {
        return reservations.stream()
                .filter(r -> r.getReservationNumber().equalsIgnoreCase(reservationNumber))
                .findFirst();
    }

    public String getNextReferenceId() {
        if (reservations.isEmpty())
            return "1001";
        int maxId = 1000;
        for (Reservation r : reservations) {
            try {
                int id = Integer.parseInt(r.getReservationNumber());
                if (id > maxId)
                    maxId = id;
            } catch (NumberFormatException ignored) {
            }
        }
        return String.valueOf(maxId + 1);
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }
}
