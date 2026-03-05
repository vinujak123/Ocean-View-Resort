package com.oceanview.resort.repository;

import com.oceanview.resort.model.PricingConfig;

public interface PricingRepository {
    /**
     * Retrieves the current pricing configuration.
     */
    PricingConfig getPricing();

    /**
     * Saves or overwrites the pricing configuration.
     */
    void savePricing(PricingConfig config);
}
