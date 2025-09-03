package com.exemple.apipagamento.portalchurras.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String password;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    // Construtor protegido para JPA
    protected User() {}

    // Construtor com validação de regras de negócio
    public User(String name, String email, String password, UserRole role) {
        this.validateBusinessRules(name, email, password);
        this.name = name.trim();
        this.email = email.toLowerCase().trim();
        this.password = password; // Senha será criptografada no service
        this.role = role != null ? role : UserRole.CUSTOMER;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Construtor simplificado para clientes
    public User(String name, String email, String password) {
        this(name, email, password, UserRole.CUSTOMER);
    }

    // Métodos de negócio
    public void updateProfile(String name, String email, String phone) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            this.validateEmail(email);
            this.email = email.toLowerCase().trim();
        }
        if (phone != null) {
            this.phone = phone.trim().isEmpty() ? null : phone.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        this.password = newPassword; // Será criptografada no service
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role não pode ser nulo");
        }
        this.role = newRole;
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

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de consulta
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isEmployee() {
        return this.role == UserRole.EMPLOYEE;
    }

    public boolean isCustomer() {
        return this.role == UserRole.CUSTOMER;
    }

    public boolean canManageMenu() {
        return this.role == UserRole.ADMIN;
    }

    public boolean canManageOrders() {
        return this.role == UserRole.ADMIN || this.role == UserRole.EMPLOYEE;
    }

    // Validação de regras de negócio
    private void validateBusinessRules(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Nome deve ter no máximo 100 caracteres");
        }

        this.validateEmail(email);

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email deve ter formato válido");
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
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public List<Order> getOrders() { return orders; }
}

