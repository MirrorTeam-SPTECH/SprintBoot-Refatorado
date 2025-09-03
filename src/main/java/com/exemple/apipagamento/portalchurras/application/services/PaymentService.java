package com.exemple.apipagamento.portalchurras.application.services;


import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.domain.ports.*;
import com.exemple.apipagamento.portalchurras.domain.usecases.PaymentUseCases;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService implements PaymentUseCases {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          PaymentGateway paymentGateway,
                          ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public Payment createPayment(Long orderId, PaymentMethod method, BigDecimal amount) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        if (method == null) {
            throw new IllegalArgumentException("Método de pagamento não pode ser nulo");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        // Verificar se já existe um pagamento para este pedido
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus().isActive()) {
            throw new IllegalStateException("Já existe um pagamento ativo para este pedido");
        }

        Payment payment = new Payment(order, method, amount);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment processPayment(Long paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("ID do pagamento não pode ser nulo");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Pagamento deve estar pendente para ser processado");
        }

        try {
            PaymentGatewayRequest request = buildPaymentRequest(payment);
            PaymentGatewayResponse response = paymentGateway.createPaymentPreference(request);

            if (response.isSuccess()) {
                payment.markAsProcessing(response.getExternalPaymentId(), response.getExternalPreferenceId());
                payment.updateExternalResponse(serializeResponse(response.getRawResponse()));
            } else {
                payment.reject(response.getErrorMessage());
            }
        } catch (Exception e) {
            payment.reject("Erro interno: " + e.getMessage());
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Payment createPixPayment(Long paymentId, String customerEmail) {
        if (paymentId == null) {
            throw new IllegalArgumentException("ID do pagamento não pode ser nulo");
        }
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email do cliente é obrigatório para PIX");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado: " + paymentId));

        if (payment.getMethod() != PaymentMethod.PIX) {
            throw new IllegalArgumentException("Pagamento deve ser do tipo PIX");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Pagamento deve estar pendente para criar PIX");
        }

        try {
            PaymentGatewayRequest request = buildPaymentRequest(payment);
            request.setCustomerEmail(customerEmail.trim());

            PaymentGatewayResponse response = paymentGateway.createPixPayment(request);

            if (response.isSuccess()) {
                payment.markAsProcessing(response.getExternalPaymentId(), response.getExternalPreferenceId());
                payment.setPixData(response.getQrCode(), response.getQrCodeBase64(), response.getTicketUrl());
                payment.updateExternalResponse(serializeResponse(response.getRawResponse()));
            } else {
                payment.reject(response.getErrorMessage());
            }
        } catch (Exception e) {
            payment.reject("Erro ao criar PIX: " + e.getMessage());
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Payment processWebhook(Map<String, Object> webhookData) {
        if (webhookData == null || webhookData.isEmpty()) {
            throw new IllegalArgumentException("Dados do webhook não podem ser nulos ou vazios");
        }

        try {
            PaymentWebhookResponse webhookResponse = paymentGateway.processWebhook(webhookData);

            if (!webhookResponse.isValid()) {
                throw new IllegalArgumentException("Webhook inválido");
            }

            Payment payment = paymentRepository.findByExternalPaymentId(webhookResponse.getExternalPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado: " + webhookResponse.getExternalPaymentId()));

            // Atualizar status baseado no webhook
            String status = webhookResponse.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "approved":
                        payment.approve();
                        break;
                    case "rejected":
                        payment.reject("Rejeitado pelo provedor");
                        break;
                    case "cancelled":
                        payment.cancel("Cancelado pelo provedor");
                        break;
                    case "expired":
                        payment.expire();
                        break;
                    default:
                        // Status desconhecido, apenas atualizar a resposta externa
                        break;
                }
            }

            payment.updateExternalResponse(serializeResponse(webhookData));
            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar webhook: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findExpiredPayments() {
        return paymentRepository.findExpiredPayments();
    }

    @Override
    public void expirePayments() {
        try {
            List<Payment> expiredPayments = findExpiredPayments();
            for (Payment payment : expiredPayments) {
                try {
                    payment.expire();
                    paymentRepository.save(payment);
                } catch (Exception e) {
                    // Log do erro mas continua processando outros pagamentos
                    System.err.println("Erro ao expirar pagamento " + payment.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar expiração de pagamentos: " + e.getMessage(), e);
        }
    }

    private PaymentGatewayRequest buildPaymentRequest(Payment payment) {
        PaymentGatewayRequest request = new PaymentGatewayRequest(
                payment.getOrder().getId(),
                payment.getMethod(),
                payment.getAmount(),
                "Pedido #" + payment.getOrder().getId(),
                payment.getOrder().getCustomerEmail()
        );

        // Converter itens do pedido se existirem
        if (payment.getOrder().getItems() != null) {
            List<PaymentGatewayRequest.PaymentItem> items = payment.getOrder().getItems().stream()
                    .map(orderItem -> new PaymentGatewayRequest.PaymentItem(
                            orderItem.getMenuItem().getName(),
                            orderItem.getQuantity(),
                            orderItem.getUnitPrice()
                    ))
                    .collect(Collectors.toList());
            request.setItems(items);
        }

        return request;
    }

    private String serializeResponse(Object response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "Erro ao serializar resposta: " + e.getMessage();
        }
    }
}