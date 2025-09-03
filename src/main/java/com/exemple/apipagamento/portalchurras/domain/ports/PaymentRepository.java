package com.exemple.apipagamento.portalchurras.domain.ports;


import com.exemple.apipagamento.portalchurras.domain.entities.Payment;
import com.exemple.apipagamento.portalchurras.domain.entities.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findExpiredPayments();
}


