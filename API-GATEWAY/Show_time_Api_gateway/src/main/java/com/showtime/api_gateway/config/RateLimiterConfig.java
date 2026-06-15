package com.showtime.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate Limiter Key Resolver configuration.
 *
 * Uses Redis Token Bucket algorithm via Spring Cloud Gateway.
 * Rate limits are applied PER IP ADDRESS.
 *
 * Config in application.yml:
 *   replenishRate:  20 requests/second (steady state)
 *   burstCapacity:  40 requests/second (burst allowed)
 *
 * Beans must be named exactly as referenced in application.yml:
 *   key-resolver: "#{@ipKeyResolver}"
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit by IP address.
     * Each unique IP gets its own token bucket.
     *
     * Primary bean — used by default rate limiter filter.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                                .getRemoteAddress() != null
                    ? exchange.getRequest()
                              .getRemoteAddress()
                              .getAddress()
                              .getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * Rate limit by authenticated user ID.
     * Used for secured endpoints — falls back to IP if no user.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just(userId);
            }
            // Fallback to IP
            String ip = exchange.getRequest()
                                .getRemoteAddress() != null
                    ? exchange.getRequest()
                              .getRemoteAddress()
                              .getAddress()
                              .getHostAddress()
                    : "anonymous";
            return Mono.just(ip);
        };
    }
}