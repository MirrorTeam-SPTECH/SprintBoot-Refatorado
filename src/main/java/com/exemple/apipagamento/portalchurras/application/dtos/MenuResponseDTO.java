package com.exemple.apipagamento.portalchurras.application.dtos;

import java.util.List;
import java.util.Map;

public class MenuResponseDTO {
    private Map<String, List<MenuItemDTO>> menu;

    public MenuResponseDTO(Map<String, List<MenuItemDTO>> menu) {
        this.menu = menu;
    }

    public Map<String, List<MenuItemDTO>> getMenu() {
        return menu;
    }

    public void setMenu(Map<String, List<MenuItemDTO>> menu) {
        this.menu = menu;
    }
}
