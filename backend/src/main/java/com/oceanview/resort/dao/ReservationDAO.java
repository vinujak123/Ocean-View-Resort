package com.oceanview.resort.dao;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.FileBasedReservationRepository;
import com.oceanview.resort.repository.MySqlReservationRepository;
import com.oceanview.resort.repository.ReservationRepository;
import com.oceanview.resort.util.DatabaseUtil;

/**
 * Data Access Object for Reservations.
 * Wraps the repository to match the training slides.
 */
public class ReservationDAO {
    private final ReservationRepository repository;

    public ReservationDAO() {
        // Match the persistence logic from ResortServer
        String persistence = System.getProperty("persistence", "auto");
        if (!"file".equalsIgnoreCase(persistence) && DatabaseUtil.testConnection()) {
            this.repository = new MySqlReservationRepository();
        } else {
            this.repository = new FileBasedReservationRepository();
        }
    }

    public boolean createReservation(Reservation res) {
        try {
            repository.save(res);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error creating reservation via DAO", e);
        }
    }
}
