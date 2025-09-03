package com.exemple.apipagamento.portalchurras.domain.usecases;


import com.exemple.apipagamento.portalchurras.domain.entities.Payment;
import com.exemple.apipagamento.portalchurras.domain.entities.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentUseCases {
    Payment createPayment(Long orderId, PaymentMethod method, BigDecimal amount);
    Payment processPayment(Long paymentId);
    Payment createPixPayment(Long paymentId, String customerEmail);
    Payment processWebhook(Map<String, Object> webhookData);
    Optional<Payment> findPaymentById(Long paymentId);
    Optional<Payment> findPaymentByOrderId(Long orderId);
    List<Payment> findExpiredPayments();
    void expirePayments();
}