package com.showtime.booking_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Booking event published to RabbitMQ.
 *
 * Published when:
 *  - Booking is confirmed (booking.confirmed)
 *  - Booking is cancelled (booking.cancelled)
 *
 * Consumed by:
 *  - Notification Service (email/SMS)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String      eventType;         // BOOKING_CONFIRMED, BOOKING_CANCELLED
    private Long        bookingId;
    private String      bookingReference;
    private Long        userId;
    private String      userEmail;
    private String      movieTitle;
    private String      theatreName;
    private String      screenName;
    private String      showDate;
    private String      showTime;
    private String      language;
    private String      format;
    private List<String> seatNumbers;
    private BigDecimal  totalAmount;
    private Integer     totalSeats;
    private String      status;
    private String      cancellationReason;
    private String      timestamp;
}