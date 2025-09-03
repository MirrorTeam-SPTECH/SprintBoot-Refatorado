package com.exemple.apipagamento.portalchurras.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pedido é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Item do menu é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private String observations;

    protected OrderItem() {}

    public OrderItem(Order order, MenuItem menuItem, Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PrePersist
    @PreUpdate
    protected void updateTotalPrice() {
        calculateTotalPrice();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public MenuItem getMenuItem() { return menuItem; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getObservations() { return observations; }

    public void setOrder(Order order) { this.order = order; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        calculateTotalPrice();
    }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }
    public void setObservations(String observations) { this.observations = observations; }
}