package com.exemple.apipagamento.portalchurras.infrastructure.repositories;


import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import com.exemple.apipagamento.portalchurras.domain.ports.MenuItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MenuItemRepositoryImpl implements MenuItemRepository {

    private final JpaMenuItemRepository jpaRepository;

    public MenuItemRepositoryImpl(JpaMenuItemRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        return jpaRepository.save(menuItem);
    }

    @Override
    public Optional<MenuItem> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<MenuItem> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<MenuItem> findByCategory(MenuCategory category) {
        return jpaRepository.findByCategory(category);
    }

    @Override
    public List<MenuItem> findByActiveTrue() {
        return jpaRepository.findAllActiveOrderedByCategoryAndName();
    }

    @Override
    public List<MenuItem> findByCategoryAndActiveTrue(MenuCategory category) {
        return jpaRepository.findByCategoryAndActiveTrue(category);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
