package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import com.exemple.apipagamento.portalchurras.domain.usecases.UserUseCases;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserUseCases {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String name, String email, String password, UserRole role) {
        // Validações de entrada
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role é obrigatória");
        }

        String normalizedEmail = email.toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Já existe um usuário com este email: " + normalizedEmail);
        }

        // Criar usuário com senha já encodada
        User user = new User(name.trim(), normalizedEmail, passwordEncoder.encode(password), role);
        return userRepository.save(user);
    }

    @Override
    public User createCustomer(String name, String email, String password, String phone) {
        // Validações de entrada
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        String normalizedEmail = email.toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Já existe um usuário com este email: " + normalizedEmail);
        }

        // Criar usuário com senha já encodada
        User user = new User(name.trim(), normalizedEmail, passwordEncoder.encode(password), UserRole.CUSTOMER);

        // Atualizar perfil com telefone se fornecido
        if (phone != null && !phone.trim().isEmpty()) {
            user.updateProfile(name.trim(), normalizedEmail, phone.trim());
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUserProfile(Long userId, String name, String email, String phone) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        // Validações dos novos dados
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode estar vazio");
        }
        if (email != null && email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode estar vazio");
        }

        // Normalizar email se fornecido
        String normalizedEmail = email != null ? email.toLowerCase().trim() : null;

        // Verificar se o novo email já existe em outro usuário
        if (normalizedEmail != null && !normalizedEmail.equals(user.getEmail()) 
                && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Já existe um usuário com este email: " + normalizedEmail);
        }

        try {
            user.updateProfile(
                name != null ? name.trim() : user.getName(),
                normalizedEmail != null ? normalizedEmail : user.getEmail(),
                phone != null ? phone.trim() : user.getPhone()
            );
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar perfil do usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public User changeUserPassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha atual é obrigatória");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Nova senha é obrigatória");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        // Verificar senha atual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        try {
            user.changePassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao alterar senha do usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        user.updateRole(newRole);
        return userRepository.save(user);
    }

    @Override
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        user.deactivate();
        return userRepository.save(user);
    }

    @Override
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        user.activate();
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + userId);
        }
        
        try {
            userRepository.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllCustomers() {
        return userRepository.findByRole(UserRole.CUSTOMER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllStaff() {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        List<User> employees = userRepository.findByRole(UserRole.EMPLOYEE);
        admins.addAll(employees);
        return admins;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Override
    public User recordUserLogin(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        String normalizedEmail = email.toLowerCase().trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + normalizedEmail));

        try {
            user.recordLogin();
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar login do usuário: " + e.getMessage(), e);
        }
    }
}

