package com.showtime.booking_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for POST /api/bookings/lock-seats
 *
 * Returns lock confirmation with:
 * - Lock TTL (seconds remaining)
 * - Total amount calculated
 * - Seat type breakdown
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeatLockResponse {

    private String          showId;
    private List<String>    lockedSeats;
    private Integer         lockTtlSeconds;   // 600 (10 min)
    private BigDecimal      totalAmount;
    private BigDecimal      convenienceFee;
    private BigDecimal      grandTotal;
    private List<SeatDetail> seatDetails;
    private String          lockExpiresAt;    // ISO datetime string
    private String          message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatDetail {
        private String     seatNumber;
        private String     seatType;
        private BigDecimal price;
    }
}