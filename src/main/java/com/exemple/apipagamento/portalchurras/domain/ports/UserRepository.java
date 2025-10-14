package com.exemple.apipagamento.portalchurras.domain.ports;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(UserRole role);
    List<User> findByActiveTrue();
    boolean existsByEmail(String email);
    void deleteById(Long id);
    long count();
    long countByRole(UserRole role);
    boolean existsById(Long userId);
}


