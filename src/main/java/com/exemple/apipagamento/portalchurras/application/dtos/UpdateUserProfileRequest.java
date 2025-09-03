package com.exemple.apipagamento.portalchurras.application.dtos;

import jakarta.validation.constraints.*;

public class UpdateUserProfileRequest {

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String name;

    @Email(message = "Email deve ter formato válido")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    public UpdateUserProfileRequest() {}

    // Getters e Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
