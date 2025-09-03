package com.exemple.apipagamento.portalchurras.infrastructure.controllers;


import com.exemple.apipagamento.portalchurras.application.dtos.*;
import com.exemple.apipagamento.portalchurras.application.mappers.UserMapper;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import com.exemple.apipagamento.portalchurras.domain.usecases.UserUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API para gerenciamento de usuários")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class UserController {

    private final UserUseCases userUseCases;
    private final UserMapper userMapper;

    public UserController(UserUseCases userUseCases, UserMapper userMapper) {
        this.userUseCases = userUseCases;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo cliente")
    @ApiResponse(responseCode = "201", description = "Cliente registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userUseCases.createCustomer(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getPhone()
            );

            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo usuário (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userUseCases.createUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole()
            );

            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário logado",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuário não autenticado"));
            }

            String email = authentication.getName();
            return userUseCases.findUserByEmail(email)
                    .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil do usuário logado",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuário não autenticado"));
            }

            User currentUser = userUseCases.findUserByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            User updatedUser = userUseCases.updateUserProfile(
                    currentUser.getId(),
                    request.getName(),
                    request.getEmail(),
                    request.getPhone()
            );

            UserDTO userDTO = userMapper.toDTO(updatedUser);
            return ResponseEntity.ok(userDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PutMapping("/me/password")
    @Operation(summary = "Alterar senha do usuário logado",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuário não autenticado"));
            }

            User currentUser = userUseCases.findUserByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            userUseCases.changeUserPassword(
                    currentUser.getId(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os usuários (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userUseCases.findAllUsers();
            List<UserDTO> userDTOs = userMapper.toDTOList(users);
            return ResponseEntity.ok(userDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(summary = "Listar clientes",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<User> customers = userUseCases.findAllCustomers();
            List<UserDTO> customerDTOs = userMapper.toDTOList(customers);
            return ResponseEntity.ok(customerDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar funcionários (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getAllStaff() {
        try {
            List<User> staff = userUseCases.findAllStaff();
            List<UserDTO> staffDTOs = userMapper.toDTOList(staff);
            return ResponseEntity.ok(staffDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuário por ID (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            return userUseCases.findUserById(userId)
                    .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar role do usuário (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> roleData) {

        try {
            String roleStr = roleData.get("role");
            if (roleStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Role é obrigatório"));
            }

            UserRole newRole = UserRole.valueOf(roleStr.toUpperCase());
            User user = userUseCases.updateUserRole(userId, newRole);
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar usuário (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            User user = userUseCases.deactivateUser(userId);
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar usuário (apenas admins)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            User user = userUseCases.activateUser(userId);
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }
}