package com.exemple.apipagamento.portalchurras.domain.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Entity
    @Table(name = "menu_items")
    public class MenuItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        @Column(nullable = false)
        private String name;

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        private String description;

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal price;

        @NotNull(message = "Categoria é obrigatória")
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private MenuCategory category;

        private String preparationTime;

        private String imageUrl;

        @Column(nullable = false)
        private Boolean active = true;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        // Construtor protegido para JPA
        protected MenuItem() {}

        // Construtor com validação de regras de negócio
        public MenuItem(String name, String description, BigDecimal price,
                        MenuCategory category, String preparationTime) {
            this.validateBusinessRules(name, price, category);
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
            this.preparationTime = preparationTime;
            this.active = true;
            this.createdAt = LocalDateTime.now();
        }

        // Métodos de negócio
        public void updateDetails(String name, String description, BigDecimal price,
                                  String preparationTime) {
            this.validateBusinessRules(name, price, this.category);
            this.name = name;
            this.description = description;
            this.price = price;
            this.preparationTime = preparationTime;
            this.updatedAt = LocalDateTime.now();
        }

        public void deactivate() {
            this.active = false;
            this.updatedAt = LocalDateTime.now();
        }

        public void activate() {
            this.active = true;
            this.updatedAt = LocalDateTime.now();
        }

        public void updateImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            this.updatedAt = LocalDateTime.now();
        }

        // Validação de regras de negócio
        private void validateBusinessRules(String name, BigDecimal price, MenuCategory category) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do item não pode estar vazio");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Preço deve ser maior que zero");
            }
            if (category == null) {
                throw new IllegalArgumentException("Categoria é obrigatória");
            }
        }

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public BigDecimal getPrice() { return price; }
        public MenuCategory getCategory() { return category; }
        public String getPreparationTime() { return preparationTime; }
        public String getImageUrl() { return imageUrl; }
        public Boolean getActive() { return active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

    }

