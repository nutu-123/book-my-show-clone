package com.showtime.api_gateway.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Gateway configuration beans.
 *
 * Routes are defined in application.yml (preferred for clarity).
 * This class handles:
 *  - Redis template bean for rate limiting
 *  - Startup logging
 */
@Configuration
public class GatewayConfig {

    private static final Logger log =
            LoggerFactory.getLogger(GatewayConfig.class);

    @Autowired(required = false)
    private RouteLocator routeLocator;

    /**
     * Reactive Redis template for rate limiter and caching.
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(
                factory,
                RedisSerializationContext.string()
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("API Gateway is ready — all routes active");
        log.info("Route table:");
        log.info("  /api/auth/**         → auth-service:8081");
        log.info("  /api/movies/**       → movie-catalog-service:8082");
        log.info("  /api/shows/**        → show-service:8083");
        log.info("  /api/theatres/**     → show-service:8083");
        log.info("  /api/bookings/**     → booking-service:8084");
        log.info("  /api/payments/**     → payment-service:8085");
        log.info("  /api/notifications/**→ notification-service:8086");
    }
}