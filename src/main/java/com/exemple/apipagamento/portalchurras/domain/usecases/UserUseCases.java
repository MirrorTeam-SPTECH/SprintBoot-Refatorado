package com.exemple.apipagamento.portalchurras.domain.usecases;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserUseCases {
    User createUser(String name, String email, String password, UserRole role);
    User createCustomer(String name, String email, String password, String phone);
    User updateUserProfile(Long userId, String name, String email, String phone);
    User changeUserPassword(Long userId, String currentPassword, String newPassword);
    User updateUserRole(Long userId, UserRole newRole);
    User deactivateUser(Long userId);
    User activateUser(Long userId);
    void deleteUser(Long userId);

    Optional<User> findUserById(Long id);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();
    List<User> findUsersByRole(UserRole role);
    List<User> findActiveUsers();
    List<User> findAllCustomers();
    List<User> findAllStaff();

    boolean existsByEmail(String email);
    User recordUserLogin(String email);
}
