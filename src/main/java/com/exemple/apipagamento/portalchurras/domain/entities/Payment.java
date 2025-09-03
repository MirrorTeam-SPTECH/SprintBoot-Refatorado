package com.exemple.apipagamento.portalchurras.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pedido é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Método de pagamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @NotNull(message = "Status do pagamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // IDs externos para rastreamento
    private String externalPaymentId; // ID do Mercado Pago
    private String externalPreferenceId; // ID da preferência MP
    private String qrCode; // QR Code para PIX
    private String qrCodeBase64; // QR Code em Base64
    private String ticketUrl; // URL do comprovante

    @Column(columnDefinition = "TEXT")
    private String externalResponse; // Resposta completa da API externa

    private String failureReason; // Motivo da falha

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;

    // Construtor protegido para JPA
    protected Payment() {}

    // Construtor para criar pagamento
    public Payment(Order order, PaymentMethod method, BigDecimal amount) {
        this.validateBusinessRules(order, method, amount);
        this.order = order;
        this.method = method;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Métodos de negócio
    public void markAsProcessing(String externalPaymentId, String externalPreferenceId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Pagamento deve estar pendente para ser processado");
        }

        this.status = PaymentStatus.PROCESSING;
        this.externalPaymentId = externalPaymentId;
        this.externalPreferenceId = externalPreferenceId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPixData(String qrCode, String qrCodeBase64, String ticketUrl) {
        if (this.method != PaymentMethod.PIX) {
            throw new IllegalStateException("Dados PIX só podem ser definidos para pagamentos PIX");
        }

        this.qrCode = qrCode;
        this.qrCodeBase64 = qrCodeBase64;
        this.ticketUrl = ticketUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve() {
        if (this.status != PaymentStatus.PENDING && this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Pagamento deve estar pendente ou processando para ser aprovado");
        }

        this.status = PaymentStatus.APPROVED;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status == PaymentStatus.APPROVED) {
            throw new IllegalStateException("Pagamento aprovado não pode ser rejeitado");
        }

        this.status = PaymentStatus.REJECTED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == PaymentStatus.APPROVED) {
            throw new IllegalStateException("Pagamento aprovado não pode ser cancelado");
        }

        this.status = PaymentStatus.CANCELLED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        if (this.status != PaymentStatus.PENDING && this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Apenas pagamentos pendentes ou processando podem expirar");
        }

        this.status = PaymentStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateExternalResponse(String response) {
        this.externalResponse = response;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateBusinessRules(Order order, PaymentMethod method, BigDecimal amount) {
        if (order == null) {
            throw new IllegalArgumentException("Pedido é obrigatório");
        }
        if (method == null) {
            throw new IllegalArgumentException("Método de pagamento é obrigatório");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.APPROVED;
    }

    public boolean canBeRetried() {
        return this.status == PaymentStatus.REJECTED || this.status == PaymentStatus.EXPIRED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getExternalPaymentId() { return externalPaymentId; }
    public String getExternalPreferenceId() { return externalPreferenceId; }
    public String getQrCode() { return qrCode; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public String getTicketUrl() { return ticketUrl; }
    public String getExternalResponse() { return externalResponse; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
}
