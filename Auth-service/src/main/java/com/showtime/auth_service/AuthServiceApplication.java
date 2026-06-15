package com.showtime.auth_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ShowTime Auth Service
 *
 * Handles:
 *  - User registration and login
 *  - JWT access token generation
 *  - Refresh token rotation
 *  - Role-based access control (RBAC)
 *  - Password encryption (BCrypt)
 *
 * Database: PostgreSQL (showtime_auth)
 * Port:     8081
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║     ShowTime - Auth Service Started          ║");
        log.info("║     Port     : 8081                          ║");
        log.info("║     Database : PostgreSQL (showtime_auth)    ║");
        log.info("║     Admin    : admin@showtime.com            ║");
        log.info("║     Password : Admin@123                     ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}