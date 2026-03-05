package com.oceanview.resort.service;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.ReservationRepository;
import com.oceanview.resort.util.ValidationUtil;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Reservation business logic
 */
public class ReservationService {

    private final ReservationRepository repository;
    private final PricingService pricingService;

    public ReservationService(ReservationRepository repository, PricingService pricingService) {
        this.repository = repository;
        this.pricingService = pricingService;
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

        // Bill Calculation logic using dynamic pricing
        long nights = ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate());
        String roomCode = res.getRoomType() != null ? res.getRoomType().name() : "STANDARD";
        String boardCode = res.getBoardType() != null ? res.getBoardType().name() : "BB";
        double dailyRate = pricingService.getRoomRate(roomCode) + pricingService.getBoardRate(boardCode);
        res.setTotalBill(nights * dailyRate);

        return repository.save(res);
    }

    public Reservation getByRefId(String refId) {
        return repository.findByReferenceId(refId).orElse(null);
    }

    public Double calculateTotalRevenue() {
        return getAll().stream().mapToDouble(Reservation::getTotalBill).sum();
    }

    public void delete(String refId) throws Exception {
        Reservation existing = getByRefId(refId);
        if (existing == null) {
            throw new Exception("Reservation not found");
        }
        repository.deleteByReferenceId(refId);
    }

    public Reservation update(String refId, Reservation updatedRes) throws Exception {
        Reservation existing = getByRefId(refId);
        if (existing == null) {
            throw new Exception("Reservation not found");
        }

        // Validate updated data
        List<String> errors = new ArrayList<>();
        ValidationUtil.validateRequired(updatedRes.getGuestName(), "Guest name", errors);
        ValidationUtil.validateRequired(updatedRes.getPhone(), "Phone number", errors);
        ValidationUtil.validateRequired(updatedRes.getCheckInDate(), "Check-in date", errors);
        ValidationUtil.validateRequired(updatedRes.getCheckOutDate(), "Check-out date", errors);

        if (!errors.isEmpty()) {
            throw new Exception(String.join(", ", errors));
        }

        if (updatedRes.getCheckOutDate().isBefore(updatedRes.getCheckInDate())
                || updatedRes.getCheckOutDate().equals(updatedRes.getCheckInDate())) {
            throw new Exception("Check-out must be at least one day after Check-in");
        }

        // Update fields
        existing.setGuestName(updatedRes.getGuestName());
        existing.setAddress(updatedRes.getAddress());
        existing.setPhone(updatedRes.getPhone());
        existing.setRoomType(updatedRes.getRoomType());
        existing.setBoardType(updatedRes.getBoardType());
        existing.setCheckInDate(updatedRes.getCheckInDate());
        existing.setCheckOutDate(updatedRes.getCheckOutDate());

        // Re-calculate bill using dynamic pricing
        long nights = ChronoUnit.DAYS.between(existing.getCheckInDate(), existing.getCheckOutDate());
        String roomCode = existing.getRoomType() != null ? existing.getRoomType().name() : "STANDARD";
        String boardCode = existing.getBoardType() != null ? existing.getBoardType().name() : "BB";
        double dailyRate = pricingService.getRoomRate(roomCode) + pricingService.getBoardRate(boardCode);
        existing.setTotalBill(nights * dailyRate);

        return repository.save(existing);
    }
}
