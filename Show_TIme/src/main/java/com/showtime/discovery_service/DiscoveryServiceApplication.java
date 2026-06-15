package com.showtime.discovery_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ShowTime Discovery Service
 *
 * Eureka Server — acts as the service registry for all
 * ShowTime microservices. Every microservice registers
 * here on startup and queries here for other services.
 *
 * Dashboard: http://localhost:8761
 * Credentials: eureka / eureka123
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(DiscoveryServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
        log.info("=========================================");
        log.info("  ShowTime Discovery Service Started     ");
        log.info("  Eureka Dashboard: http://localhost:8761");
        log.info("  Credentials:      eureka / eureka123   ");
        log.info("=========================================");
    }
}