package com.showtime.discovery_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Dashboard.
 *
 * Allows:
 *  - Browser access to Eureka dashboard with basic auth
 *  - All Eureka client registrations (they use basic auth in URL)
 *  - Actuator health/info endpoints without auth
 *
 * CSRF is disabled because Eureka clients use REST calls,
 * not browser form submissions.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF — required for Eureka client registrations
            .csrf(csrf -> csrf.disable())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow actuator health and info without login
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Enable HTTP Basic auth for Eureka dashboard
            .httpBasic(httpBasic -> {});

        return http.build();
    }
}