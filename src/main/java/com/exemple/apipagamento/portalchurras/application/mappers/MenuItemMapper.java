package com.exemple.apipagamento.portalchurras.application.mappers;

import com.exemple.apipagamento.portalchurras.application.dtos.MenuItemDTO;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuItemMapper {

    public MenuItemDTO toDTO(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }

        return new MenuItemDTO(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getPreparationTime(),
                menuItem.getImageUrl(),
                menuItem.getActive()
        );
    }

    public List<MenuItemDTO> toDTOList(List<MenuItem> menuItems) {
        return menuItems.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}