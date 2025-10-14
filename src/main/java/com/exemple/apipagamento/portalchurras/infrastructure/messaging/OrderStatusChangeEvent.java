package com.exemple.apipagamento.portalchurras.infrastructure.messaging;

import com.exemple.apipagamento.portalchurras.domain.entities.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de mudança de status do pedido.
 * Utilizado para comunicação assíncrona via RabbitMQ quando o status de um pedido é alterado.
 * 
 * @author Portal Churras Team
 * @since 1.0
 */
public class OrderStatusChangeEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String customerEmail;
    private String customerName;
    private BigDecimal orderTotal;
    private LocalDateTime changedAt;
    private String changedBy;
    private String reason;

    /**
     * Construtor padrão necessário para serialização.
     */
    public OrderStatusChangeEvent() {
        this.changedAt = LocalDateTime.now();
    }

    /**
     * Construtor completo com todos os campos obrigatórios.
     * 
     * @param orderId ID do pedido
     * @param oldStatus Status anterior do pedido
     * @param newStatus Novo status do pedido
     * @param customerEmail Email do cliente
     * @param customerName Nome do cliente
     * @param orderTotal Valor total do pedido
     */
    public OrderStatusChangeEvent(Long orderId, OrderStatus oldStatus, OrderStatus newStatus,
                                   String customerEmail, String customerName, BigDecimal orderTotal) {
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.orderTotal = orderTotal;
        this.changedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(OrderStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "OrderStatusChangeEvent{" +
                "orderId=" + orderId +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", orderTotal=" + orderTotal +
                ", changedAt=" + changedAt +
                ", changedBy='" + changedBy + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
