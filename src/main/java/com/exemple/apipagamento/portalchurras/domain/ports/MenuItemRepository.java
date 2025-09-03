package com.exemple.apipagamento.portalchurras.domain.ports;

import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository {
    MenuItem save(MenuItem menuItem);
    Optional<MenuItem> findById(Long id);
    List<MenuItem> findAll();
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByActiveTrue();
    List<MenuItem> findByCategoryAndActiveTrue(MenuCategory category);
    boolean existsByName(String name);
    void deleteById(Long id);
}