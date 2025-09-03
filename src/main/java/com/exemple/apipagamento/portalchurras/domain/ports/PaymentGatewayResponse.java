package com.exemple.apipagamento.portalchurras.domain.ports;

import java.util.Map;

public class PaymentGatewayResponse {
    private boolean success;
    private String externalPaymentId;
    private String externalPreferenceId;
    private String initPoint;
    private String qrCode;
    private String qrCodeBase64;
    private String ticketUrl;
    private String status;
    private String errorMessage;
    private Map<String, Object> rawResponse;

    public PaymentGatewayResponse() {}

    public static PaymentGatewayResponse success(String externalPaymentId) {
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.success = true;
        response.externalPaymentId = externalPaymentId;
        return response;
    }

    public static PaymentGatewayResponse error(String errorMessage) {
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }

    // Getters e Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }

    public String getExternalPreferenceId() { return externalPreferenceId; }
    public void setExternalPreferenceId(String externalPreferenceId) { this.externalPreferenceId = externalPreferenceId; }

    public String getInitPoint() { return initPoint; }
    public void setInitPoint(String initPoint) { this.initPoint = initPoint; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }

    public String getTicketUrl() { return ticketUrl; }
    public void setTicketUrl(String ticketUrl) { this.ticketUrl = ticketUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, Object> getRawResponse() { return rawResponse; }
    public void setRawResponse(Map<String, Object> rawResponse) { this.rawResponse = rawResponse; }
}
