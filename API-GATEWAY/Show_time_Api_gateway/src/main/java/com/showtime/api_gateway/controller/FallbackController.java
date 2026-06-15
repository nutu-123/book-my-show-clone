package com.showtime.api_gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker.
 *
 * When a downstream service is unavailable,
 * Spring Cloud Gateway routes to these fallback endpoints
 * instead of returning a raw connection error.
 *
 * Configured in application.yml via:
 *   fallbackUri: forward:/fallback/{service}
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log =
            LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return buildFallback("auth-service",
                "Authentication service is temporarily unavailable.");
    }

    @GetMapping("/movie")
    public ResponseEntity<Map<String, Object>> movieFallback() {
        return buildFallback("movie-catalog-service",
                "Movie catalog service is temporarily unavailable.");
    }

    @GetMapping("/show")
    public ResponseEntity<Map<String, Object>> showFallback() {
        return buildFallback("show-service",
                "Show service is temporarily unavailable.");
    }

    @GetMapping("/booking")
    public ResponseEntity<Map<String, Object>> bookingFallback() {
        return buildFallback("booking-service",
                "Booking service is temporarily unavailable. " +
                "Your seat selection has NOT been confirmed.");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return buildFallback("payment-service",
                "Payment service is temporarily unavailable. " +
                "Please do NOT retry payment — contact support.");
    }

    private ResponseEntity<Map<String, Object>> buildFallback(
            String service, String message) {
        log.warn("Circuit breaker fallback triggered for: {}", service);

        Map<String, Object> response = new HashMap<>();
        response.put("success",   false);
        response.put("message",   message);
        response.put("error",     "SERVICE_UNAVAILABLE");
        response.put("service",   service);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                             .body(response);
    }
}