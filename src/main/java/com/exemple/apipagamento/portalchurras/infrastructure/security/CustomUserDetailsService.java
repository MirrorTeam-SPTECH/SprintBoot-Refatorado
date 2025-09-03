package com.exemple.apipagamento.portalchurras.infrastructure.security;

import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.usecases.UserUseCases;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserUseCases userUseCases;

    public CustomUserDetailsService(UserUseCases userUseCases) {
        this.userUseCases = userUseCases;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userUseCases.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!user.getActive()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        return new CustomUserPrincipal(user);
    }

    // Classe interna para representar o usuário autenticado
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            String roleName = "ROLE_" + user.getRole().name();
            return Collections.singletonList(new SimpleGrantedAuthority(roleName));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getActive();
        }

        // Método para acessar o usuário completo
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }

        public String getUserRole() {
            return user.getRole().name();
        }
    }
}
