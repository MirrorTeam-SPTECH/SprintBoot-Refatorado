package com.exemple.apipagamento.portalchurras.application.dtos;

import jakarta.validation.constraints.*;

public class ChangePasswordRequest {

    @NotBlank(message = "Senha atual é obrigatória")
    private String currentPassword;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Nova senha deve ter pelo menos 6 caracteres")
    private String newPassword;

    public ChangePasswordRequest() {}

    // Getters e Setters
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
