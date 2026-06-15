package com.showtime.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request Tracking Filter.
 *
 * Assigns a unique X-Correlation-Id to every request.
 * This ID flows through all downstream services,
 * enabling distributed tracing and log correlation.
 *
 * Order: -2 (runs FIRST, before JWT filter)
 */
@Component
public class RequestTrackingFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(RequestTrackingFilter.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public int getOrder() {
        return -2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // Reuse existing correlation ID if present (from client or upstream)
        String correlationId = request.getHeaders()
                                      .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        log.debug("Correlation-Id: {}", finalCorrelationId);

        // Add to request (forwarded to downstream services)
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, finalCorrelationId)
                .build();

        // Also add to response (client can use for support requests)
        exchange.getResponse()
                .getHeaders()
                .add(CORRELATION_ID_HEADER, finalCorrelationId);

        return chain.filter(
                exchange.mutate().request(mutatedRequest).build()
        );
    }
}