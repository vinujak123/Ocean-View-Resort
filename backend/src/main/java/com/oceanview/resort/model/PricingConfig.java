package com.oceanview.resort.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Pricing Configuration Model
 */
public class PricingConfig {

    private List<Plan> roomPlans = new ArrayList<>();
    private List<Plan> boardPlans = new ArrayList<>();
    private List<Offer> seasonalOffers = new ArrayList<>();

    // Default Constructor
    public PricingConfig() {
    }

    public static class Plan {
        private String code;
        private String name;
        private Double rate;

        public Plan() {
        }

        public Plan(String code, String name, Double rate) {
            this.code = code;
            this.name = name;
            this.rate = rate;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getRate() {
            return rate;
        }

        public void setRate(Double rate) {
            this.rate = rate;
        }
    }

    public static class Offer {
        private String code;
        private String description;
        private Double discountPercentage; // e.g., 10.0 for 10% off

        public Offer() {
        }

        public Offer(String code, String description, Double discountPercentage) {
            this.code = code;
            this.description = description;
            this.discountPercentage = discountPercentage;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Double getDiscountPercentage() {
            return discountPercentage;
        }

        public void setDiscountPercentage(Double discountPercentage) {
            this.discountPercentage = discountPercentage;
        }
    }

    public List<Plan> getRoomPlans() {
        return roomPlans;
    }

    public void setRoomPlans(List<Plan> roomPlans) {
        this.roomPlans = roomPlans;
    }

    public List<Plan> getBoardPlans() {
        return boardPlans;
    }

    public void setBoardPlans(List<Plan> boardPlans) {
        this.boardPlans = boardPlans;
    }

    public List<Offer> getSeasonalOffers() {
        return seasonalOffers;
    }

    public void setSeasonalOffers(List<Offer> seasonalOffers) {
        this.seasonalOffers = seasonalOffers;
    }
}
