package com.exemple.apipagamento.portalchurras.application.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateOrderRequest {

    // Para usuários registrados
    private Long customerId;

    // Para pedidos de convidados (quando customerId é null)
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String customerName;

    @Email(message = "Email deve ter formato válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String customerEmail;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String customerPhone;

    @DecimalMin(value = "0.0", message = "Total não pode ser negativo")
    private BigDecimal total = BigDecimal.ZERO;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String notes;

    public CreateOrderRequest() {}

    // Getters e Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Método de validação
    public boolean isValid() {
        // Se há customerId, não precisa dos dados de guest
        if (customerId != null) {
            return true;
        }
        // Se não há customerId, pelo menos o nome é obrigatório
        return customerName != null && !customerName.trim().isEmpty();
    }
}