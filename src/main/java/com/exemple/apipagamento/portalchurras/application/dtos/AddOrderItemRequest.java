package com.exemple.apipagamento.portalchurras.application.dtos;

import jakarta.validation.constraints.*;

public class AddOrderItemRequest {

    @NotNull(message = "ID do item do menu é obrigatório")
    private Long menuItemId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Max(value = 100, message = "Quantidade não pode exceder 100")
    private Integer quantity;

    @Size(max = 255, message = "Observações devem ter no máximo 255 caracteres")
    private String observations;

    public AddOrderItemRequest() {}

    public AddOrderItemRequest(Long menuItemId, Integer quantity, String observations) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.observations = observations;
    }

    // Getters e Setters
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}