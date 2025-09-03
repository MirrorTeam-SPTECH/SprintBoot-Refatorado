package com.exemple.apipagamento.portalchurras.infrastructure.repositories;


import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByActiveTrue();
    List<MenuItem> findByCategoryAndActiveTrue(MenuCategory category);
    boolean existsByName(String name);

    @Query("SELECT m FROM MenuItem m WHERE m.active = true ORDER BY m.category, m.name")
    List<MenuItem> findAllActiveOrderedByCategoryAndName();
}


