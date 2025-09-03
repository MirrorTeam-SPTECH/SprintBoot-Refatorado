package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.usecases.UserUseCases;
import com.exemple.apipagamento.portalchurras.infrastructure.security.JwtUtil;
import com.exemple.apipagamento.portalchurras.infrastructure.security.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API para autenticação")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserUseCases userUseCases;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          CustomUserDetailsService userDetailsService,
                          UserUseCases userUseCases) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userUseCases = userUseCases;
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Carregar detalhes do usuário
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                    (CustomUserDetailsService.CustomUserPrincipal) userDetails;

            // Registrar login
            userUseCases.recordUserLogin(loginRequest.getEmail());

            // Gerar token
            String token = jwtUtil.generateTokenWithClaims(
                    userPrincipal.getUsername(),
                    userPrincipal.getUserRole(),
                    userPrincipal.getUserId()
            );

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "type", "Bearer",
                    "expiresAt", LocalDateTime.now().plusDays(1),
                    "user", Map.of(
                            "id", userPrincipal.getUserId(),
                            "name", userPrincipal.getUser().getName(),
                            "email", userPrincipal.getUsername(),
                            "role", userPrincipal.getUserRole()
                    )
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email ou senha incorretos"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token inválido"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null && jwtUtil.validateToken(token, username)) {
                User user = userUseCases.findUserByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                String newToken = jwtUtil.generateTokenWithClaims(
                        user.getEmail(),
                        user.getRole().name(),
                        user.getId()
                );

                return ResponseEntity.ok(Map.of(
                        "token", newToken,
                        "type", "Bearer",
                        "expiresAt", LocalDateTime.now().plusDays(1)
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Erro ao renovar token"));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("valid", false, "error", "Token inválido"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null && jwtUtil.validateToken(token, username)) {
                User user = userUseCases.findUserByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "user", Map.of(
                                "id", user.getId(),
                                "name", user.getName(),
                                "email", user.getEmail(),
                                "role", user.getRole().name()
                        )
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Token expirado"));
            }

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", "Token inválido"));
        }
    }

    // DTO interno para login
    public static class LoginRequest {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ter formato válido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
        private String password;

        public LoginRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}