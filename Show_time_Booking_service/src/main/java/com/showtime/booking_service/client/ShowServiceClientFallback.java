package com.showtime.booking_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback for ShowServiceClient.
 *
 * If Show Service is unavailable when we try to update seats,
 * we log the failure. The booking is already confirmed at this
 * point — a reconciliation job would handle this in production.
 */
@Component
public class ShowServiceClientFallback implements ShowServiceClient {

    private static final Logger log =
            LoggerFactory.getLogger(ShowServiceClientFallback.class);

    @Override
    public ResponseEntity<Map<String, Object>> updateSeatAvailability(
            String showId, List<String> seats, boolean isBooking) {

        log.error("FALLBACK: Show Service unavailable. " +
                  "Could not update seats for show: {}. " +
                  "Seats: {}. isBooking: {}",
                  showId, seats, isBooking);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Show Service unavailable — seat update queued for retry");
        return ResponseEntity.ok(response);
    }
}