package com.showtime.booking_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ShowTime Booking Service
 *
 * Handles:
 *  - Distributed seat locking via Redis (NX + TTL)
 *  - Booking creation with transaction handling
 *  - Ticket generation per seat
 *  - Booking confirmation after payment
 *  - Booking cancellation with seat release
 *  - Scheduled cleanup of expired pending bookings
 *
 * Database: PostgreSQL (showtime_booking)
 * Cache:    Redis (seat locks)
 * Events:   RabbitMQ (booking.confirmed, booking.cancelled)
 * Port:     8084
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class BookingServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(BookingServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║     ShowTime - Booking Service Started       ║");
        log.info("║     Port     : 8084                          ║");
        log.info("║     Database : PostgreSQL (showtime_booking) ║");
        log.info("║     Cache    : Redis (seat locking)          ║");
        log.info("║     Events   : RabbitMQ                      ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}