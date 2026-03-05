package com.oceanview.resort.service;

import com.oceanview.resort.model.PricingConfig;
import com.oceanview.resort.repository.PricingRepository;

public class PricingService {
    private final PricingRepository repository;

    public PricingService(PricingRepository repository) {
        this.repository = repository;
    }

    public PricingConfig getPricing() {
        return repository.getPricing();
    }

    public void updatePricing(PricingConfig newConfig) {
        // Here we could add validation (e.g. no negative prices, required names)
        repository.savePricing(newConfig);
    }

    public Double getRoomRate(String code) {
        PricingConfig config = getPricing();
        if (config.getRoomPlans() != null) {
            for (PricingConfig.Plan p : config.getRoomPlans()) {
                if (p.getCode().equalsIgnoreCase(code))
                    return p.getRate();
            }
        }
        return 0.0;
    }

    public Double getBoardRate(String code) {
        PricingConfig config = getPricing();
        if (config.getBoardPlans() != null) {
            for (PricingConfig.Plan p : config.getBoardPlans()) {
                if (p.getCode().equalsIgnoreCase(code))
                    return p.getRate();
            }
        }
        return 0.0;
    }
}
