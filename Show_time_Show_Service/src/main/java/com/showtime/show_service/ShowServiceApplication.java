package com.showtime.show_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * ShowTime Show Service
 *
 * Handles:
 *  - Theatre CRUD (Admin)
 *  - Screen management with seat layouts (Admin)
 *  - Show scheduling (Admin)
 *  - Show queries by movie, city, date (Public)
 *  - Seat layout retrieval for booking (Public)
 *
 * Database: MongoDB (showtime_shows)
 * Port:     8083
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing
public class ShowServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(ShowServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShowServiceApplication.class, args);
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║     ShowTime - Show Service Started          ║");
        log.info("║     Port     : 8083                          ║");
        log.info("║     Database : MongoDB (showtime_shows)      ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}
