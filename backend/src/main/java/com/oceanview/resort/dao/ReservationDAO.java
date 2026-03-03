package com.oceanview.resort.dao;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.MySqlReservationRepository;
import com.oceanview.resort.repository.ReservationRepository;

/**
 * Data Access Object for Reservations.
 * Wraps the repository to match the training slides.
 */
public class ReservationDAO {
    private final ReservationRepository repository;

    public ReservationDAO() {
        this.repository = new MySqlReservationRepository();
    }

    public boolean createReservation(Reservation res) {
        try {
            repository.save(res);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
