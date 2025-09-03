package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import com.exemple.apipagamento.portalchurras.domain.ports.MenuItemRepository;
import com.exemple.apipagamento.portalchurras.domain.usecases.MenuItemUseCases;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MenuItemService implements MenuItemUseCases {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public MenuItem createMenuItem(String name, String description, BigDecimal price,
                                   MenuCategory category, String preparationTime) {

        // Validação de regra de negócio - nome único
        if (menuItemRepository.existsByName(name)) {
            throw new IllegalArgumentException("Já existe um item com este nome: " + name);
        }

        MenuItem menuItem = new MenuItem(name, description, price, category, preparationTime);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem updateMenuItem(Long id, String name, String description,
                                   BigDecimal price, String preparationTime) {

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));

        // Verificar se o novo nome já existe em outro item
        if (!menuItem.getName().equals(name) && menuItemRepository.existsByName(name)) {
            throw new IllegalArgumentException("Já existe um item com este nome: " + name);
        }

        menuItem.updateDetails(name, description, price, preparationTime);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Item não encontrado: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    @Override
    public void deactivateMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));

        menuItem.deactivate();
        menuItemRepository.save(menuItem);
    }

    @Override
    public void activateMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));

        menuItem.activate();
        menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem updateMenuItemImage(Long id, String imageUrl) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));

        menuItem.updateImageUrl(imageUrl);
        return menuItemRepository.save(menuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MenuItem> findMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItem> findAllActiveMenuItems() {
        return menuItemRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItem> findMenuItemsByCategory(MenuCategory category) {
        return menuItemRepository.findByCategoryAndActiveTrue(category);
    }
}
