package com.exemple.apipagamento.portalchurras.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions")
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loyalty_program_id", nullable = false)
    private LoyaltyProgram loyaltyProgram;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer balanceBefore;

    @Column(nullable = false)
    private Integer balanceAfter;

    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        EARNED("Pontos Ganhos"),
        REDEEMED("Pontos Resgatados"),
        BONUS("Bônus"),
        EXPIRED("Pontos Expirados"),
        ADJUSTMENT("Ajuste");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Constructors
    public LoyaltyTransaction() {}

    public LoyaltyTransaction(LoyaltyProgram loyaltyProgram, Order order, 
                             TransactionType type, Integer points, String description) {
        this.loyaltyProgram = loyaltyProgram;
        this.order = order;
        this.type = type;
        this.points = points;
        this.balanceBefore = loyaltyProgram.getAvailablePoints();
        this.balanceAfter = type == TransactionType.REDEEMED ? 
            balanceBefore - points : balanceBefore + points;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public LoyaltyProgram getLoyaltyProgram() { return loyaltyProgram; }
    public Order getOrder() { return order; }
    public TransactionType getType() { return type; }
    public Integer getPoints() { return points; }
    public Integer getBalanceBefore() { return balanceBefore; }
    public Integer getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}