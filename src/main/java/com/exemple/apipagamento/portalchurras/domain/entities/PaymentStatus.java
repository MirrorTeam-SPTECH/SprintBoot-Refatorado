package com.exemple.apipagamento.portalchurras.domain.entities;

public enum PaymentStatus {
    PENDING("Pendente"),
    PROCESSING("Processando"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    CANCELLED("Cancelado"),
    EXPIRED("Expirado"),
    REFUNDED("Reembolsado");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == PROCESSING;
    }

    public boolean isFinalized() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }
}
