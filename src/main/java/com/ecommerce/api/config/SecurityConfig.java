package com.ecommerce.api.config;

import com.ecommerce.api.security.jwt.JwtAuthEntryPoint;
import com.ecommerce.api.security.jwt.JwtAuthenticationFilter;
import com.ecommerce.api.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Spring Security configuration.
 *
 * - Stateless JWT-based authentication (no sessions).
 * - Role-based access control via @PreAuthorize or endpoint-level rules.
 * - BCrypt password encoding.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize, @Secured, @RolesAllowed
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ---- Public endpoints that do NOT require a token ----
    private static final String[] PUBLIC_URLS = {
            "/auth/**",
            "/products/**",
            "/categories/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Return JSON 401 instead of redirect for unauthenticated requests
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))

            // Stateless session — JWT handles auth, no server-side session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                    // Public: auth endpoints, product/category browsing, Swagger
                    .requestMatchers(PUBLIC_URLS).permitAll()

                    // Admin-only endpoints
                    .requestMatchers(HttpMethod.POST,   "/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
                    .requestMatchers("/admin/**").hasRole("ADMIN")

                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )

            // Register DaoAuthenticationProvider (username/password + BCrypt)
            .authenticationProvider(authenticationProvider())

            // Run JWT filter before Spring Security's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication provider — wires our UserDetailsService + PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expose AuthenticationManager as a bean so AuthServiceImpl can inject it.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder — industry standard for hashing passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
