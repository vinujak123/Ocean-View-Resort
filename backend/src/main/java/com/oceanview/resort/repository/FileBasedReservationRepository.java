package com.oceanview.resort.repository;

import com.google.gson.reflect.TypeToken;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.util.JsonUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * File-based repository for Reservation persistence using JSON
 */
public class FileBasedReservationRepository {

    private static final String DATA_FILE = "backend/data/reservations.json";
    private final Path dataPath;
    private final AtomicLong idCounter;

    public FileBasedReservationRepository() {
        this.dataPath = Paths.get(DATA_FILE);
        this.idCounter = new AtomicLong(0);
        initializeDataFile();
    }

    /**
     * Initialize data file if it doesn't exist
     */
    private void initializeDataFile() {
        try {
            if (!Files.exists(dataPath.getParent())) {
                Files.createDirectories(dataPath.getParent());
            }
            if (!Files.exists(dataPath)) {
                Files.writeString(dataPath, "[]");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data file", e);
        }
    }

    /**
     * Find all reservations
     */
    public List<Reservation> findAll() {
        try {
            String json = Files.readString(dataPath);
            Type listType = new TypeToken<ArrayList<Reservation>>() {
            }.getType();
            List<Reservation> reservations = JsonUtil.getGson().fromJson(json, listType);
            return reservations != null ? reservations : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read reservations", e);
        }
    }

    /**
     * Save a reservation (create or update)
     */
    public synchronized Reservation save(Reservation reservation) {
        try {
            List<Reservation> reservations = findAll();

            // Update existing or add new
            if (reservation.getId() != null) {
                // Update existing
                reservations.removeIf(r -> r.getId().equals(reservation.getId()));
            } else {
                // Assign new ID
                reservation.setId(idCounter.incrementAndGet());
            }

            reservations.add(reservation);

            // Write back to file
            String json = JsonUtil.toJson(reservations);
            Files.writeString(dataPath, json);

            return reservation;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reservation", e);
        }
    }

    /**
     * Find reservation by reference ID
     */
    public Optional<Reservation> findByReferenceId(String referenceId) {
        return findAll().stream()
                .filter(r -> r.getReferenceId().equals(referenceId))
                .findFirst();
    }

    /**
     * Find maximum reference ID for auto-increment
     */
    public String findMaxReferenceId() {
        return findAll().stream()
                .map(Reservation::getReferenceId)
                .filter(id -> id != null && id.matches("\\d+"))
                .max(String::compareTo)
                .orElse(null);
    }
}
