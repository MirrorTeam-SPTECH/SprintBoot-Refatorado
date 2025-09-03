package com.exemple.apipagamento.portalchurras.infrastructure.config;

import com.exemple.apipagamento.portalchurras.infrastructure.security.JwtAuthenticationFilter;
import com.exemple.apipagamento.portalchurras.infrastructure.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/h2-console/**",
                                "/api/auth/**",
                                "/api/users/register",  // Registro público de clientes
                                "/api/webhooks/**"      // Webhooks públicos
                        ).permitAll()

                        // Menu items - leitura pública, escrita apenas admin
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/menu-items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menu-items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/menu-items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu-items/**").hasRole("ADMIN")

                        // Orders - criação pública, gerenciamento por staff
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/items").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/*").permitAll()  // Consulta de pedido específico
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/cancel").permitAll()  // Cancelamento pelo cliente
                        .requestMatchers("/api/orders/**").hasAnyRole("EMPLOYEE", "ADMIN")

                        // Payments - criação pública, consulta restrita
                        .requestMatchers(HttpMethod.POST, "/api/payments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/process").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/pix").permitAll()
                        .requestMatchers("/api/payments/**").hasAnyRole("EMPLOYEE", "ADMIN")

                        // Users - próprio perfil vs gestão de outros
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/customers").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Qualquer outra requisição precisa de autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
