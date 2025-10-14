package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Construtor correto: User(String name, String email, String password, UserRole role)
        testUser = new User("Test User", "test@example.com", "password123", UserRole.CUSTOMER);
        // Usar ReflectionTestUtils para definir ID (não há setter público)
        ReflectionTestUtils.setField(testUser, "id", 1L);
        // Adicionar telefone via método de negócio
        testUser.updateProfile("Test User", "test@example.com", "11999999999");
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - createUser tem 4 parâmetros (sem telefone)
        User result = userService.createUser("Test User", "test@example.com", 
                                            "password123", UserRole.CUSTOMER);

        // Then
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("Test User", "test@example.com", 
                                  "password123", UserRole.CUSTOMER);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveCriarClienteComSucesso() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createCustomer("Test User", "test@example.com", 
                                                "password123", "11999999999");

        // Then
        assertNotNull(result);
        assertEquals(UserRole.CUSTOMER, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveBuscarUsuarioPorId() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findUserByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void deveAtualizarPerfilDoUsuario() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - updateUserProfile tem 4 parâmetros: userId, name, email, phone
        // Usando o mesmo email para não acionar a verificação de email duplicado
        User result = userService.updateUserProfile(1L, "Updated Name", "test@example.com", "11988888888");

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveAtualizarPerfilComNovoEmail() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - mudando o email
        User result = userService.updateUserProfile(1L, "Updated Name", "newemail@example.com", "11988888888");

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).existsByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(999L, "Updated Name", "test@example.com", "11988888888");
        });
    }

    @Test
    void deveDesativarUsuario() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.deactivateUser(1L);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveAtivarUsuario() {
        // Given
        testUser.deactivate();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.activateUser(1L);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveAlterarSenha() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - Método correto é changeUserPassword
        User result = userService.changeUserPassword(1L, "password123", "newPassword456");

        // Then
        assertNotNull(result);
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoSenhaAtualIncorreta() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changeUserPassword(1L, "wrongPassword", "newPassword456");
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveRegistrarLogin() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordUserLogin("test@example.com");

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }
}
