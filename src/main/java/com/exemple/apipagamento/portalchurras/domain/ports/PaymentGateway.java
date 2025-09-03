package com.exemple.apipagamento.portalchurras.domain.ports;


import java.math.BigDecimal;
import java.util.Map;


public interface PaymentGateway {


    PaymentGatewayResponse createPaymentPreference(PaymentGatewayRequest request);

    PaymentGatewayResponse createPixPayment(PaymentGatewayRequest request);

    PaymentGatewayResponse getPaymentStatus(String externalPaymentId);

    PaymentGatewayResponse cancelPayment(String externalPaymentId);

    PaymentWebhookResponse processWebhook(Map<String, Object> webhookData);
}