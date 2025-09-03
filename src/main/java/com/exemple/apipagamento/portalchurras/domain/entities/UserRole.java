package com.exemple.apipagamento.portalchurras.domain.entities;

public enum UserRole {
    ADMIN("Administrador"),
    EMPLOYEE("Funcionário"),
    CUSTOMER("Cliente");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canManageMenu() {
        return this == ADMIN;
    }

    public boolean canManageOrders() {
        return this == ADMIN || this == EMPLOYEE;
    }

    public boolean canViewReports() {
        return this == ADMIN || this == EMPLOYEE;
    }

    public boolean isStaff() {
        return this == ADMIN || this == EMPLOYEE;
    }
}