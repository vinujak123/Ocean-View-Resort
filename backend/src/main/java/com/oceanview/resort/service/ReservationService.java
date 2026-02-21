package com.oceanview.resort.service;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.FileBasedReservationRepository;
import com.oceanview.resort.util.ValidationUtil;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Reservation business logic
 */
public class ReservationService {

    private final FileBasedReservationRepository repository;

    public ReservationService(FileBasedReservationRepository repository) {
        this.repository = repository;
    }

    public List<Reservation> getAll() {
        return repository.findAll();
    }

    public Reservation create(Reservation res) throws Exception {
        // Manual validation
        List<String> errors = new ArrayList<>();
        ValidationUtil.validateRequired(res.getGuestName(), "Guest name", errors);
        ValidationUtil.validateRequired(res.getPhone(), "Phone number", errors);
        ValidationUtil.validateRequired(res.getCheckInDate(), "Check-in date", errors);
        ValidationUtil.validateRequired(res.getCheckOutDate(), "Check-out date", errors);

        if (!errors.isEmpty()) {
            throw new Exception(String.join(", ", errors));
        }

        // Business Validation
        if (res.getCheckOutDate().isBefore(res.getCheckInDate())
                || res.getCheckOutDate().equals(res.getCheckInDate())) {
            throw new Exception("Check-out must be at least one day after Check-in");
        }

        // Auto-increment ID logic
        String maxId = repository.findMaxReferenceId();
        int nextId = (maxId == null) ? 1001 : Integer.parseInt(maxId) + 1;
        res.setReferenceId(String.valueOf(nextId));

        // Bill Calculation logic
        long nights = ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate());
        double dailyRate = res.getRoomType().rate + res.getBoardType().rate;
        res.setTotalBill(nights * dailyRate);

        return repository.save(res);
    }

    public Reservation getByRefId(String refId) {
        return repository.findByReferenceId(refId).orElse(null);
    }

    public Double calculateTotalRevenue() {
        return getAll().stream().mapToDouble(Reservation::getTotalBill).sum();
    }
}
