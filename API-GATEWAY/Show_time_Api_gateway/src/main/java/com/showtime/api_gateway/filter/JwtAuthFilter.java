package com.showtime.api_gateway.filter;

import com.showtime.api_gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Global JWT Authentication Filter.
 *
 * Runs on EVERY request through the Gateway.
 * Logic:
 *  1. If the request is OPTIONS (preflight) → allow through
 *  2. If the path is in publicEndpoints → allow through
 *  3. Otherwise → extract and validate JWT
 *     → If valid → add userId and roles to downstream headers
 *     → If invalid → return 401 Unauthorized
 *
 * Order: -1 (runs before routing filters)
 */
@Component
@ConfigurationProperties(prefix="gateway")
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    // Injected from application.yml
    private List<String> publicEndpoints;

    @Override
    public int getOrder() {
        // Run before all other filters
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path   = request.getURI().getPath();
        String method = request.getMethod().name();

        log.debug("Gateway filter processing: {} {}", method, path);

        // ── 1. Allow CORS preflight through immediately ──
        if (HttpMethod.OPTIONS.matches(method)) {
            log.debug("OPTIONS preflight — passing through");
            return chain.filter(exchange);
        }

        // ── 2. Check if path is public ──
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint — skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        // ── 3. Extract Authorization header ──
        String authHeader = request.getHeaders()
                                   .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return sendUnauthorized(exchange,
                    "Authorization header missing or malformed");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // ── 4. Validate token ──
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid JWT token for path: {}", path);
            return sendUnauthorized(exchange, "Invalid or expired JWT token");
        }

        // ── 5. Extract claims and add to downstream headers ──
        try {
            String userId   = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);

            log.debug("JWT valid — userId: {}, username: {}, roles: {}",
                    userId, username, roles);

            // Mutate request to add user info headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id",    userId != null ? userId : "")
                    .header("X-User-Email", username != null ? username : "")
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate()
                                        .request(mutatedRequest)
                                        .build());

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            return sendUnauthorized(exchange, "Token processing failed");
        }
    }

    /**
     * Check if the incoming path matches any public endpoint pattern.
     * Supports Ant-style wildcards (e.g., /api/movies/**)
     */
    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Write a 401 Unauthorized response with a JSON body.
     * Uses reactive Netty buffer — no blocking.
     */
    private Mono<Void> sendUnauthorized(ServerWebExchange exchange,
                                         String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
            "{\"success\":false,\"message\":\"%s\"," +
            "\"error\":\"UNAUTHORIZED\"," +
            "\"timestamp\":\"%s\"}",
            message,
            LocalDateTime.now()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}