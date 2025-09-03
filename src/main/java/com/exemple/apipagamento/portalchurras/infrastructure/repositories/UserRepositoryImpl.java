package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaRepository;

    public UserRepositoryImpl(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User não pode ser nulo");
        }
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return jpaRepository.findByEmail(email.toLowerCase().trim());
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role não pode ser nulo");
        }
        return jpaRepository.findActiveUsersByRoleOrderByName(role);
    }

    @Override
    public List<User> findByActiveTrue() {
        return jpaRepository.findByActiveTrue();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return jpaRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (!jpaRepository.existsById(id)) {
            throw new IllegalArgumentException("User com ID " + id + " não encontrado");
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByRole(UserRole role) {
        if (role == null) {
            return 0L;
        }
        return jpaRepository.countByRole(role);
    }

    // Métodos adicionais úteis (não fazem parte da interface domain)
    public List<User> findAllStaffUsers() {
        return jpaRepository.findAllStaffUsers();
    }

    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return jpaRepository.findByNameOrEmailContaining(searchTerm.trim());
    }
}