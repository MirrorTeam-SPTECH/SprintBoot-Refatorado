package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.MenuItemDTO;
import com.exemple.apipagamento.portalchurras.application.dtos.MenuResponseDTO;
import com.exemple.apipagamento.portalchurras.application.mappers.MenuItemMapper;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuItem;
import com.exemple.apipagamento.portalchurras.domain.usecases.MenuItemUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu-items")
@Tag(name = "Menu Items", description = "API para gerenciamento do cardápio")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class MenuItemController {

    private final MenuItemUseCases menuItemUseCases;
    private final MenuItemMapper menuItemMapper;

    public MenuItemController(MenuItemUseCases menuItemUseCases, MenuItemMapper menuItemMapper) {
        this.menuItemUseCases = menuItemUseCases;
        this.menuItemMapper = menuItemMapper;
    }

    @GetMapping
    @Operation(summary = "Listar todos os itens ativos do cardápio",
            description = "Retorna todos os itens ativos organizados por categoria")
    @ApiResponse(responseCode = "200", description = "Lista de itens recuperada com sucesso")
    public ResponseEntity<MenuResponseDTO> getAllActiveMenuItems() {
        try {
            List<MenuItem> menuItems = menuItemUseCases.findAllActiveMenuItems();
            List<MenuItemDTO> menuItemDTOs = menuItemMapper.toDTOList(menuItems);

            // Organizar por categoria
            Map<String, List<MenuItemDTO>> menuByCategory = menuItemDTOs.stream()
                    .collect(Collectors.groupingBy(item -> item.getCategory().name().toLowerCase()));

            return ResponseEntity.ok(new MenuResponseDTO(menuByCategory));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar item por ID")
    @ApiResponse(responseCode = "200", description = "Item encontrado")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ResponseEntity<MenuItemDTO> getMenuItemById(
            @Parameter(description = "ID do item") @PathVariable Long id) {

        return menuItemUseCases.findMenuItemById(id)
                .map(menuItem -> ResponseEntity.ok(menuItemMapper.toDTO(menuItem)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Buscar itens por categoria")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemsByCategory(
            @Parameter(description = "Categoria do item") @PathVariable MenuCategory category) {

        try {
            List<MenuItem> menuItems = menuItemUseCases.findMenuItemsByCategory(category);
            List<MenuItemDTO> menuItemDTOs = menuItemMapper.toDTOList(menuItems);
            return ResponseEntity.ok(menuItemDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Apenas admins podem criar itens
    @Operation(summary = "Criar novo item do cardápio",
            security = @SecurityRequirement(name = "Bearer"))
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Item com nome duplicado")
    public ResponseEntity<?> createMenuItem(@Valid @RequestBody MenuItemDTO menuItemDTO) {
        try {
            MenuItem menuItem = menuItemUseCases.createMenuItem(
                    menuItemDTO.getName(),
                    menuItemDTO.getDescription(),
                    menuItemDTO.getPrice(),
                    menuItemDTO.getCategory(),
                    menuItemDTO.getPreparationTime()
            );

            MenuItemDTO responseDTO = menuItemMapper.toDTO(menuItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar item do cardápio",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemDTO menuItemDTO) {

        try {
            MenuItem menuItem = menuItemUseCases.updateMenuItem(
                    id,
                    menuItemDTO.getName(),
                    menuItemDTO.getDescription(),
                    menuItemDTO.getPrice(),
                    menuItemDTO.getPreparationTime()
            );

            MenuItemDTO responseDTO = menuItemMapper.toDTO(menuItem);
            return ResponseEntity.ok(responseDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar imagem do item",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> updateMenuItemImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> imageData) {

        try {
            String imageUrl = imageData.get("imageUrl");
            MenuItem menuItem = menuItemUseCases.updateMenuItemImage(id, imageUrl);

            MenuItemDTO responseDTO = menuItemMapper.toDTO(menuItem);
            return ResponseEntity.ok(responseDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar item do cardápio",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> deactivateMenuItem(@PathVariable Long id) {
        try {
            menuItemUseCases.deactivateMenuItem(id);
            return ResponseEntity.ok(Map.of("message", "Item desativado com sucesso"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar item do cardápio",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> activateMenuItem(@PathVariable Long id) {
        try {
            menuItemUseCases.activateMenuItem(id);
            return ResponseEntity.ok(Map.of("message", "Item ativado com sucesso"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir item do cardápio permanentemente",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemUseCases.deleteMenuItem(id);
            return ResponseEntity.ok(Map.of("message", "Item excluído com sucesso"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }
}