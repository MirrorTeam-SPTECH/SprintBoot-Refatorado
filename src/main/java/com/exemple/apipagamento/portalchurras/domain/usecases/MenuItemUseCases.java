package com.exemple.apipagamento.portalchurras.domain.usecases;

import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MenuItemUseCases {
    MenuItem createMenuItem(String name, String description, BigDecimal price,
                            MenuCategory category, String preparationTime);
    MenuItem updateMenuItem(Long id, String name, String description,
                            BigDecimal price, String preparationTime);
    void deleteMenuItem(Long id);
    void deactivateMenuItem(Long id);
    void activateMenuItem(Long id);
    MenuItem updateMenuItemImage(Long id, String imageUrl);
    Optional<MenuItem> findMenuItemById(Long id);
    List<MenuItem> findAllActiveMenuItems();
    List<MenuItem> findMenuItemsByCategory(MenuCategory category);
}
