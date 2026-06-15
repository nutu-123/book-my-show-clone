package com.showtime.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global Logging Filter.
 *
 * Logs every request and response passing through the gateway.
 * Measures response time for performance monitoring.
 *
 * Order: 0 (runs after JWT filter, before routing)
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        long startTime = Instant.now().toEpochMilli();

        String requestId = request.getId();
        String method    = request.getMethod().name();
        String path      = request.getURI().getPath();
        String clientIp  = getClientIp(request);

        log.info("→ REQUEST  [{}] {} {} from {}",
                requestId, method, path, clientIp);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = Instant.now().toEpochMilli() - startTime;
            int statusCode = response.getStatusCode() != null
                    ? response.getStatusCode().value()
                    : 0;

            log.info("← RESPONSE [{}] {} {} → {} ({}ms)",
                    requestId, method, path, statusCode, duration);
        }));
    }

    /**
     * Extract real client IP — checks X-Forwarded-For header first
     * (set by load balancers/proxies), falls back to remote address.
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders()
                                      .getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
}