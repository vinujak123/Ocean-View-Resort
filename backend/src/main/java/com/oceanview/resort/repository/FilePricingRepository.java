package com.oceanview.resort.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oceanview.resort.model.PricingConfig;
import java.io.*;
import java.util.Arrays;

public class FilePricingRepository implements PricingRepository {

    private final String filePath;
    private final Gson gson;

    public FilePricingRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                // Write default config
                PricingConfig defaultConfig = new PricingConfig();
                defaultConfig.setRoomPlans(Arrays.asList(
                        new PricingConfig.Plan("STANDARD", "Standard", 15000.0),
                        new PricingConfig.Plan("DELUXE", "Deluxe", 25000.0),
                        new PricingConfig.Plan("SUITE", "Suite", 45000.0)));
                defaultConfig.setBoardPlans(Arrays.asList(
                        new PricingConfig.Plan("BB", "Bed & Breakfast", 0.0),
                        new PricingConfig.Plan("HB", "Half Board", 5000.0),
                        new PricingConfig.Plan("FB", "Full Board", 10000.0)));
                savePricing(defaultConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PricingConfig getPricing() {
        try (Reader reader = new FileReader(filePath)) {
            PricingConfig config = gson.fromJson(reader, PricingConfig.class);
            return config != null ? config : new PricingConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return new PricingConfig();
        }
    }

    @Override
    public void savePricing(PricingConfig config) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save pricing configuration", e);
        }
    }
}
