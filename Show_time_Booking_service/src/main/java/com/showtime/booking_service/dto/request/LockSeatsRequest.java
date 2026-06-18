package com.showtime.booking_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for POST /api/bookings/lock-seats
 *
 * Step 1 of booking flow:
 * User selects seats on frontend → frontend calls this endpoint
 * → seats are locked in Redis for 10 minutes
 * → user proceeds to payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsRequest {

    @NotBlank(message = "Show ID is required")
    private String showId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 10, message = "Maximum 10 seats per booking")
    private List<String> seatNumbers;
}