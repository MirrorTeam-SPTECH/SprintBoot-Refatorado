package com.exemple.apipagamento.portalchurras.application.dtos;

import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class MenuItemDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal price;

    @NotNull(message = "Categoria é obrigatória")
    private MenuCategory category;

    private String preparationTime;
    private String imageUrl;
    private Boolean active;

    // Construtor padrão
    public MenuItemDTO() {}

    // Construtores
    public MenuItemDTO(Long id, String name, String description, BigDecimal price,
                       MenuCategory category, String preparationTime, String imageUrl, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.preparationTime = preparationTime;
        this.imageUrl = imageUrl;
        this.active = active;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }

    public String getPreparationTime() { return preparationTime; }
    public void setPreparationTime(String preparationTime) { this.preparationTime = preparationTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

