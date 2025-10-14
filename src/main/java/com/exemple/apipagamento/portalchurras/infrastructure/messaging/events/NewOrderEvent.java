package com.exemple.apipagamento.portalchurras.infrastructure.messaging.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NewOrderEvent implements Serializable {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal total;
    private LocalDateTime createdAt;

    // Constructors
    public NewOrderEvent() {}

    public NewOrderEvent(Long orderId, Long customerId, String customerName, 
                        String customerEmail, BigDecimal total) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.total = total;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}