package com.exemple.apipagamento.portalchurras.application.mappers;

import com.exemple.apipagamento.portalchurras.application.dtos.UserDTO;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());

        // Contar pedidos
        if (user.getOrders() != null) {
            dto.setTotalOrders((long) user.getOrders().size());
        } else {
            dto.setTotalOrders(0L);
        }

        return dto;
    }

    public List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
