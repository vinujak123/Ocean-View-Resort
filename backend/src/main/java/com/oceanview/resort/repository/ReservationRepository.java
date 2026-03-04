package com.oceanview.resort.repository;

import com.oceanview.resort.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    Optional<Reservation> findByReferenceId(String referenceId);

    String findMaxReferenceId();

    Optional<Reservation> findById(Long id);

    void deleteByReferenceId(String referenceId);
}
