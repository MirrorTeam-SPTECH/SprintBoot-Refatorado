package com.exemple.apipagamento.portalchurras.domain.entities;

public enum MenuCategory {
    COMBOS("Combos"),
    HAMBURGUERES("Hambúrgueres"),
    ESPETINHOS("Espetinhos"),
    PORCOES("Porções"),
    BEBIDAS("Bebidas"),
    ADICIONAIS("Adicionais");

    private final String displayName;

    MenuCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}