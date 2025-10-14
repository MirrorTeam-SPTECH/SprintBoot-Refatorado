package com.exemple.apipagamento.portalchurras.domain.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_programs")
public class LoyaltyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer totalPoints = 0;

    @Column(nullable = false)
    private Integer availablePoints = 0;

    @Column(nullable = false)
    private Integer usedPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyTier tier = LoyaltyTier.BRONZE;

    @Column(nullable = false)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalOrders = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastActivityAt;

    private LocalDateTime tierUpgradeAt;

    // Constructor
    public LoyaltyProgram() {}

    public LoyaltyProgram(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    // Business Methods
    public void addPoints(Integer points, BigDecimal orderValue) {
        if (points <= 0) {
            throw new IllegalArgumentException("Pontos devem ser positivos");
        }
        
        this.totalPoints += points;
        this.availablePoints += points;
        this.totalSpent = this.totalSpent.add(orderValue);
        this.totalOrders++;
        this.lastActivityAt = LocalDateTime.now();
        
        checkTierUpgrade();
    }

    public void usePoints(Integer points) {
        if (points > this.availablePoints) {
            throw new IllegalStateException("Pontos insuficientes");
        }
        
        this.availablePoints -= points;
        this.usedPoints += points;
        this.lastActivityAt = LocalDateTime.now();
    }

    private void checkTierUpgrade() {
        LoyaltyTier newTier = calculateTier();
        if (newTier != this.tier && newTier.ordinal() > this.tier.ordinal()) {
            this.tier = newTier;
            this.tierUpgradeAt = LocalDateTime.now();
        }
    }

    private LoyaltyTier calculateTier() {
        if (totalPoints >= 5000 || totalSpent.compareTo(new BigDecimal("5000")) >= 0) {
            return LoyaltyTier.DIAMOND;
        } else if (totalPoints >= 2500 || totalSpent.compareTo(new BigDecimal("2500")) >= 0) {
            return LoyaltyTier.GOLD;
        } else if (totalPoints >= 1000 || totalSpent.compareTo(new BigDecimal("1000")) >= 0) {
            return LoyaltyTier.SILVER;
        }
        return LoyaltyTier.BRONZE;
    }

    public BigDecimal getDiscountPercentage() {
        return tier.getDiscountPercentage();
    }

    public Integer getPointsMultiplier() {
        return tier.getPointsMultiplier();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getTotalPoints() { return totalPoints; }
    public Integer getAvailablePoints() { return availablePoints; }
    public Integer getUsedPoints() { return usedPoints; }
    public LoyaltyTier getTier() { return tier; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public Integer getTotalOrders() { return totalOrders; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public LocalDateTime getTierUpgradeAt() { return tierUpgradeAt; }
}