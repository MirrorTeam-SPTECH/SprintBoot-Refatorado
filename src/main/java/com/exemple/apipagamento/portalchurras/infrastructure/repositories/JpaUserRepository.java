package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByActiveTrue();

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.active = true AND u.role = :role ORDER BY u.name")
    List<User> findActiveUsersByRoleOrderByName(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    List<User> findByNameOrEmailContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM User u WHERE u.role IN ('ADMIN', 'EMPLOYEE') ORDER BY u.role, u.name")
    List<User> findAllStaffUsers();
}