package com.showtime.movie_catalog_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * ShowTime Movie Catalog Service
 *
 * Handles:
 *  - Movie CRUD (Admin)
 *  - Movie search and filtering
 *  - Genre management
 *  - Trending movies
 *  - Review system
 *
 * Database: MongoDB (showtime_movies)
 * Port:     8082
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing
public class MovieCatalogServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(MovieCatalogServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MovieCatalogServiceApplication.class, args);
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║   ShowTime - Movie Catalog Service Started   ║");
        log.info("║   Port     : 8082                            ║");
        log.info("║   Database : MongoDB (showtime_movies)       ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}