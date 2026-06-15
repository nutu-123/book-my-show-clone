package com.showtime.api_gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the API Gateway.
 *
 * Applied globally — all downstream services inherit this.
 * Frontend dev server runs on port 5173 (Vite default).
 *
 * NOTE: Spring Cloud Gateway is WebFlux-based (reactive),
 * so we use reactive CorsWebFilter, NOT the MVC CorsFilter.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins — add your production domain here later
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",    // Vite dev server
                "http://localhost:3000",    // Alternative dev port
                "http://127.0.0.1:5173"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE",
                "PATCH", "OPTIONS", "HEAD"
        ));

        // Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies / Authorization header
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        // Expose these headers to the frontend JavaScript
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Correlation-Id",
                "X-User-Id"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}