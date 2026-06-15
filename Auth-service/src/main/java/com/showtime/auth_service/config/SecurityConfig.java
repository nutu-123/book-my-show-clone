package com.showtime.auth_service.config;


import com.showtime.auth_service.security.CustomUserDetailsService;
import com.showtime.auth_service.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
 * Spring Security configuration for Auth Service.
 *
 * Key decisions:
 *  - STATELESS sessions (JWT handles state)
 *  - CSRF disabled (API — no browser form submissions)
 *  - Public endpoints: /api/auth/register, /api/auth/login, /api/auth/refresh
 *  - All other endpoints require valid JWT
 *  - BCrypt for password hashing (strength 12)
 *  - Method-level security enabled (@PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF — REST API, no browser form sessions
            .csrf(AbstractHttpConfigurer::disable)

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no JWT needed
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/actuator/**"
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/users/admin/**")
                    .hasAuthority("ROLE_ADMIN")

                // All other requests need authentication
                .anyRequest().authenticated()
            )

            // Stateless — no HTTP session
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Use our custom AuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter BEFORE username/password filter
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * BCrypt password encoder with strength 12.
     * Higher strength = more secure but slower hashing.
     * 12 is a good balance for production.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider — loads user from DB
     * and compares BCrypt-hashed passwords.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * AuthenticationManager — used in AuthService to
     * authenticate login requests programmatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}