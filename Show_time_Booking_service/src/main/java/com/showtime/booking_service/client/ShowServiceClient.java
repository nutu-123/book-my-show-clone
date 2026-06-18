package com.showtime.booking_service.client;

import com.showtime.booking_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Show Service.
 *
 * Used by Booking Service to:
 *  1. Update seat availability after booking confirmed
 *  2. Release seats after booking cancelled
 *
 * Service discovery: lb://show-service
 * (Eureka resolves to actual host:port)
 */
@FeignClient(
    name           = "show-service",
    configuration  = FeignConfig.class,
    fallback       = ShowServiceClientFallback.class
)
public interface ShowServiceClient {

    /**
     * Update booked seats in Show Service.
     * Called after booking is confirmed or cancelled.
     *
     * isBooking=true  → mark seats as booked
     * isBooking=false → release seats (cancellation)
     */
    @PutMapping("/api/shows/{showId}/seats")
    ResponseEntity<Map<String, Object>> updateSeatAvailability(
            @PathVariable("showId") String showId,
            @RequestParam("seats") List<String> seats,
            @RequestParam("isBooking") boolean isBooking
    );
}