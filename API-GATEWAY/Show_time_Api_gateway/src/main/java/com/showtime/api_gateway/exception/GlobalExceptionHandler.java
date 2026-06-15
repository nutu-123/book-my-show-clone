package com.showtime.api_gateway.exception;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.showtime.api_gateway.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Global exception handler for the API Gateway.
 *
 * Catches exceptions thrown during routing/filtering
 * and returns clean JSON error responses.
 *
 * Order: -1 (high priority, runs before default handler)
 */
@Order(-1)
@Configuration
public class GlobalExceptionHandler implements WebExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;
        String error;

        // Map exception types to HTTP statuses
        if (ex instanceof NotFoundException) {
            status  = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service is currently unavailable. Please try again.";
            error   = "SERVICE_UNAVAILABLE";
            log.error("Service not found: {}", ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status  = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null
                    ? rse.getReason()
                    : "Request processing failed";
            error   = "REQUEST_ERROR";
            log.warn("Response status exception: {}", ex.getMessage());

        } else if (ex instanceof java.net.ConnectException) {
            status  = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Cannot connect to the service. Please try later.";
            error   = "CONNECTION_REFUSED";
            log.error("Connection refused: {}", ex.getMessage());

        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            status  = HttpStatus.GATEWAY_TIMEOUT;
            message = "Service request timed out. Please try again.";
            error   = "GATEWAY_TIMEOUT";
            log.error("Timeout: {}", ex.getMessage());

        } else {
            status  = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred.";
            error   = "INTERNAL_SERVER_ERROR";
            log.error("Unhandled gateway exception: ", ex);
        }

        return writeErrorResponse(exchange, status, message, error);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange,
                                           HttpStatus status,
                                           String message,
                                           String error) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                message,
                error,
                LocalDateTime.now().toString()
        );

        String body;
        try {
            body = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            body = "{\"success\":false,\"message\":\"Error serializing response\"}";
        }

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}