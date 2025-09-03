package com.exemple.apipagamento.portalchurras.domain.entities;

public enum OrderStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    IN_PREPARATION("Em Preparação"),
    READY("Pronto"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this != DELIVERED && this != CANCELLED;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }
}