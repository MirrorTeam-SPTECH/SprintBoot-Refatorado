package com.exemple.apipagamento.portalchurras.domain.ports;

public class PaymentWebhookResponse {
    private boolean valid;
    private String externalPaymentId;
    private String status;
    private String action;
    private String type;

    public PaymentWebhookResponse() {}

    public PaymentWebhookResponse(boolean valid, String externalPaymentId, String status) {
        this.valid = valid;
        this.externalPaymentId = externalPaymentId;
        this.status = status;
    }

    // Getters e Setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
