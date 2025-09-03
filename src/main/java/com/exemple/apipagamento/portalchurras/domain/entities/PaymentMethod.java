package com.exemple.apipagamento.portalchurras.domain.entities;

public enum PaymentMethod {
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    PIX("PIX"),
    BOLETO("Boleto"),
    CASH("Dinheiro");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isInstant() {
        return this == PIX || this == CREDIT_CARD || this == DEBIT_CARD || this == CASH;
    }

    public boolean requiresExternalProcessing() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PIX || this == BOLETO;
    }
}