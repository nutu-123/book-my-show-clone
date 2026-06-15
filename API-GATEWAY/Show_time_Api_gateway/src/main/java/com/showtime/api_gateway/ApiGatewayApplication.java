package com.showtime.api_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ShowTime API Gateway
 *
 * Single entry point for all ShowTime microservices.
 * Handles:
 *  - JWT validation
 *  - Request routing
 *  - Rate limiting (Redis)
 *  - CORS
 *  - Logging
 *  - Circuit breaking (fallbacks)
 *
 * All client requests hit port 8080 and are routed
 * to the appropriate downstream microservice.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    private static final Logger log =
            LoggerFactory.getLogger(ApiGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║     ShowTime - API Gateway Started           ║");
        log.info("║     Port    : 8080                           ║");
        log.info("║     Routes  : /api/auth, /api/movies,        ║");
        log.info("║               /api/shows, /api/bookings,     ║");
        log.info("║               /api/payments                  ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}