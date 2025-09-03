package com.exemple.apipagamento.portalchurras.domain.ports;

import com.exemple.apipagamento.portalchurras.domain.entities.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PaymentGatewayRequest {
    private Long orderId;
    private PaymentMethod method;
    private BigDecimal amount;
    private String description;
    private String customerEmail;
    private List<PaymentItem> items;
    private Map<String, String> backUrls;
    private Map<String, Object> additionalData;

    public PaymentGatewayRequest() {}

    public PaymentGatewayRequest(Long orderId, PaymentMethod method, BigDecimal amount,
                                 String description, String customerEmail) {
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.description = description;
        this.customerEmail = customerEmail;
    }

    // Getters e Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<PaymentItem> getItems() { return items; }
    public void setItems(List<PaymentItem> items) { this.items = items; }

    public Map<String, String> getBackUrls() { return backUrls; }
    public void setBackUrls(Map<String, String> backUrls) { this.backUrls = backUrls; }

    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }

    public static class PaymentItem {
        private String title;
        private Integer quantity;
        private BigDecimal unitPrice;

        public PaymentItem() {}

        public PaymentItem(String title, Integer quantity, BigDecimal unitPrice) {
            this.title = title;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters e Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}