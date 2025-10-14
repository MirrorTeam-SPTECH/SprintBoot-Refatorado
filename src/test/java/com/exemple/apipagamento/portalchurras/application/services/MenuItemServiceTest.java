package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.domain.ports.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        // Construtor correto: MenuItem(String name, String description, BigDecimal price, MenuCategory category, String preparationTime)
        testMenuItem = new MenuItem(
            "X-Burger",
            "Hambúrguer artesanal",
            new BigDecimal("25.00"),
            MenuCategory.HAMBURGUERES,
            "15 min"
        );
        // Usar ReflectionTestUtils para definir ID
        ReflectionTestUtils.setField(testMenuItem, "id", 1L);
    }

    @Test
    void deveCriarMenuItemComSucesso() {
        // Given
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testMenuItem);

        // When
        MenuItem result = menuItemService.createMenuItem(
            "X-Burger",
            "Hambúrguer artesanal",
            new BigDecimal("25.00"),
            MenuCategory.HAMBURGUERES,
            "15 min"
        );

        // Then
        assertNotNull(result);
        assertEquals("X-Burger", result.getName());
        assertEquals(new BigDecimal("25.00"), result.getPrice());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void deveAtualizarMenuItemComSucesso() {
        // Given
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testMenuItem);

        // When
        MenuItem result = menuItemService.updateMenuItem(
            1L,
            "X-Burger Premium",
            "Hambúrguer artesanal premium",
            new BigDecimal("30.00"),
            "20 min"
        );

        // Then
        assertNotNull(result);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarItemInexistente() {
        // Given
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            menuItemService.updateMenuItem(
                999L,
                "Item",
                "Description",
                new BigDecimal("10.00"),
                "10 min"
            );
        });
    }

    @Test
    void deveDesativarMenuItem() {
        // Given
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));

        // When
        menuItemService.deactivateMenuItem(1L);

        // Then - Verificar que foi desativado usando método de negócio
        assertFalse(testMenuItem.getActive());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void deveAtivarMenuItem() {
        // Given
        testMenuItem.deactivate(); // Usar método de negócio
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));

        // When
        menuItemService.activateMenuItem(1L);

        // Then
        assertTrue(testMenuItem.getActive());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void deveAtualizarImagemDoMenuItem() {
        // Given
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testMenuItem);

        // When
        MenuItem result = menuItemService.updateMenuItemImage(1L, "https://example.com/image.jpg");

        // Then
        assertNotNull(result);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void deveBuscarMenuItemPorId() {
        // Given
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));

        // When
        Optional<MenuItem> result = menuItemService.findMenuItemById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void deveBuscarTodosItensAtivos() {
        // Given
        List<MenuItem> items = Arrays.asList(testMenuItem);
        when(menuItemRepository.findByActiveTrue()).thenReturn(items);

        // When
        List<MenuItem> result = menuItemService.findAllActiveMenuItems();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(menuItemRepository, times(1)).findByActiveTrue();
    }

    @Test
    void deveBuscarItensPorCategoria() {
        // Given
        List<MenuItem> items = Arrays.asList(testMenuItem);
        when(menuItemRepository.findByCategoryAndActiveTrue(MenuCategory.HAMBURGUERES))
            .thenReturn(items);

        // When
        List<MenuItem> result = menuItemService.findMenuItemsByCategory(MenuCategory.HAMBURGUERES);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MenuCategory.HAMBURGUERES, result.get(0).getCategory());
    }

    @Test
    void deveDeletarMenuItem() {
        // Given
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        doNothing().when(menuItemRepository).deleteById(1L);

        // When
        menuItemService.deleteMenuItem(1L);

        // Then
        verify(menuItemRepository, times(1)).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoDeletarItemInexistente() {
        // Given
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            menuItemService.deleteMenuItem(999L);
        });
    }
}
