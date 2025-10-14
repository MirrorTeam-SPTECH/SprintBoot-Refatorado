package com.exemple.apipagamento.portalchurras.domain.entities;

import java.math.BigDecimal;

public enum LoyaltyTier {
    BRONZE("Bronze", new BigDecimal("0"), 1, 0),
    SILVER("Prata", new BigDecimal("5"), 2, 1000),
    GOLD("Ouro", new BigDecimal("10"), 3, 2500),
    DIAMOND("Diamante", new BigDecimal("15"), 4, 5000);

    private final String displayName;
    private final BigDecimal discountPercentage;
    private final Integer pointsMultiplier;
    private final Integer requiredPoints;

    LoyaltyTier(String displayName, BigDecimal discountPercentage, 
               Integer pointsMultiplier, Integer requiredPoints) {
        this.displayName = displayName;
        this.discountPercentage = discountPercentage;
        this.pointsMultiplier = pointsMultiplier;
        this.requiredPoints = requiredPoints;
    }

    public String getDisplayName() { return displayName; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public Integer getPointsMultiplier() { return pointsMultiplier; }
    public Integer getRequiredPoints() { return requiredPoints; }
}