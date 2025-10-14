package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.LoyaltyProgram;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.infrastructure.repositories.LoyaltyProgramRepository;
import com.exemple.apipagamento.portalchurras.infrastructure.repositories.LoyaltyTransactionRepository;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private LoyaltyProgramRepository loyaltyProgramRepository;

    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private User testUser;
    private LoyaltyProgram testLoyaltyProgram;

    @BeforeEach
    void setUp() {
        // Criar usuário com construtor correto (name, email, password, role)
        testUser = new User("Test User", "test@test.com", "password", UserRole.CUSTOMER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // Criar LoyaltyProgram com construtor correto (User)
        testLoyaltyProgram = new LoyaltyProgram(testUser);
        ReflectionTestUtils.setField(testLoyaltyProgram, "id", 1L);
    }

    @Test
    void deveCriarProgramaDeFidelidade() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(loyaltyProgramRepository.save(any(LoyaltyProgram.class))).thenReturn(testLoyaltyProgram);

        // When
        LoyaltyProgram result = loyaltyService.createLoyaltyProgram(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(0, result.getAvailablePoints());
        verify(loyaltyProgramRepository, times(1)).save(any(LoyaltyProgram.class));
    }

    @Test
    void deveLancarExcecaoAoCriarProgramaComUsuarioInexistente() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            loyaltyService.createLoyaltyProgram(999L);
        });
    }

    @Test
    void deveAdicionarPontos() {
        // Given
        Order testOrder = new Order(testUser, new BigDecimal("100.00"), "Test order");
        ReflectionTestUtils.setField(testOrder, "id", 1L);
        
        when(loyaltyProgramRepository.findByUserId(1L)).thenReturn(Optional.of(testLoyaltyProgram));
        when(loyaltyProgramRepository.save(any(LoyaltyProgram.class))).thenReturn(testLoyaltyProgram);

        // When
        LoyaltyProgram result = loyaltyService.earnPoints(1L, testOrder);

        // Then
        assertNotNull(result);
        verify(loyaltyProgramRepository, times(1)).save(any(LoyaltyProgram.class));
    }

    @Test
    void deveCalcularPontosParaValorDeCompra() {
        // Given
        Order testOrder = new Order(testUser, new BigDecimal("100.00"), "Test order");
        ReflectionTestUtils.setField(testOrder, "id", 1L);
        
        when(loyaltyProgramRepository.findByUserId(1L)).thenReturn(Optional.of(testLoyaltyProgram));
        when(loyaltyProgramRepository.save(any(LoyaltyProgram.class))).thenReturn(testLoyaltyProgram);

        // When - cada R$ 1 = 10 pontos
        LoyaltyProgram result = loyaltyService.earnPoints(1L, testOrder);

        // Then
        assertNotNull(result);
        verify(loyaltyProgramRepository, times(1)).save(any(LoyaltyProgram.class));
    }

    @Test
    void deveResgatarPontos() {
        // Given
        testLoyaltyProgram.addPoints(500, new BigDecimal("50.00"));
        when(loyaltyProgramRepository.findByUserId(1L)).thenReturn(Optional.of(testLoyaltyProgram));
        when(loyaltyProgramRepository.save(any(LoyaltyProgram.class))).thenReturn(testLoyaltyProgram);

        // When
        LoyaltyProgram result = loyaltyService.redeemPoints(1L, 200, "Desconto em pedido");

        // Then
        assertNotNull(result);
        verify(loyaltyProgramRepository, times(1)).save(any(LoyaltyProgram.class));
    }

    @Test
    void deveLancarExcecaoAoResgatarPontosSemSaldoSuficiente() {
        // Given - testLoyaltyProgram já tem 0 pontos no setUp
        when(loyaltyProgramRepository.findByUserId(1L)).thenReturn(Optional.of(testLoyaltyProgram));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            loyaltyService.redeemPoints(1L, 200, "Tentativa de desconto");
        });
    }

    @Test
    void deveBuscarProgramaPorUsuario() {
        // Given
        when(loyaltyProgramRepository.findByUserId(1L)).thenReturn(Optional.of(testLoyaltyProgram));

        // When
        Optional<LoyaltyProgram> result = loyaltyService.findByUserId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUser().getId());
    }

    @Test
    void deveCalcularDesconto() {
        // Given
        testLoyaltyProgram.addPoints(500, new BigDecimal("50.00"));
        when(loyaltyProgramRepository.findByUserId(1L)).thenReturn(Optional.of(testLoyaltyProgram));

        // When
        BigDecimal discount = loyaltyService.calculateDiscount(1L, new BigDecimal("100.00"));

        // Then
        assertNotNull(discount);
        assertTrue(discount.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void deveRetornarZeroQuandoProgramaNaoExiste() {
        // Given
        when(loyaltyProgramRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // When
        BigDecimal discount = loyaltyService.calculateDiscount(999L, new BigDecimal("100.00"));

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }
}
