package com.showtime.discovery_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health endpoint for the Discovery Service.
 * Supplements Spring Actuator with a human-friendly response.
 *
 * GET /discovery/health → public (no auth needed, see SecurityConfig)
 */
@RestController
@RequestMapping("/discovery")
public class HealthController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private int port;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service",   appName);
        response.put("status",    "UP");
        response.put("port",      port);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message",   "ShowTime Discovery Service is running");
        return ResponseEntity.ok(response);
    }
}