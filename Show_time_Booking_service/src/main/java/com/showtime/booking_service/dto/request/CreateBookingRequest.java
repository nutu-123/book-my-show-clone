package com.showtime.booking_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for POST /api/bookings
 *
 * Step 2 of booking flow:
 * After payment is initiated, create the booking record.
 * Seats must already be locked via /lock-seats.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotBlank(message = "Show ID is required")
    private String showId;

    @NotEmpty(message = "Seat numbers are required")
    @Size(max = 10, message = "Maximum 10 seats per booking")
    private List<String> seatNumbers;

    // Optional — passed from frontend after show detail fetch
    private String movieId;
    private String movieTitle;
    private String theatreId;
    private String theatreName;
    private String screenId;
    private String screenName;
    private String showDate;
    private String showTime;
    private String language;
    private String format;
}