package com.showtime.booking_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Thrown when one or more seats are already locked or booked.
 * Contains the list of unavailable seats for UI feedback.
 */
@ResponseStatus(HttpStatus.CONFLICT)
@Getter
public class SeatNotAvailableException extends RuntimeException {

    private final List<String> unavailableSeats;

    public SeatNotAvailableException(String message,
                                      List<String> unavailableSeats) {
        super(message);
        this.unavailableSeats = unavailableSeats;
    }
}