package com.showtime.booking_service.service;

import com.showtime.booking_service.constants.AppConstants;
import com.showtime.booking_service.entity.Booking;
import com.showtime.booking_service.entity.Ticket;
import com.showtime.booking_service.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Publishes booking events to RabbitMQ.
 *
 * Events:
 *  - BOOKING_CONFIRMED → booking.confirmed routing key
 *  - BOOKING_CANCELLED → booking.cancelled routing key
 *
 * Consumed by:
 *  - Notification Service (sends email/SMS)
 */
@Service
public class BookingEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(BookingEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Publish booking confirmed event.
     * Triggered after payment success.
     */
    public void publishBookingConfirmed(Booking booking) {
        BookingEvent event = buildEvent(booking, "BOOKING_CONFIRMED");
        publishEvent(event, AppConstants.BOOKING_CONFIRMED_ROUTING);
        log.info("Published BOOKING_CONFIRMED event for booking: {}",
                booking.getBookingReference());
    }

    /**
     * Publish booking cancelled event.
     * Triggered when user cancels or booking expires.
     */
    public void publishBookingCancelled(Booking booking) {
        BookingEvent event = buildEvent(booking, "BOOKING_CANCELLED");
        publishEvent(event, AppConstants.BOOKING_CANCELLED_ROUTING);
        log.info("Published BOOKING_CANCELLED event for booking: {}",
                booking.getBookingReference());
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    private void publishEvent(BookingEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                    AppConstants.EXCHANGE_NAME,
                    routingKey,
                    event
            );
        } catch (Exception e) {
            // Log but don't fail the booking operation
            // The booking is confirmed in DB — notification can be retried
            log.error("Failed to publish event to RabbitMQ: {}. " +
                      "Booking still confirmed in DB.", e.getMessage());
        }
    }

    private BookingEvent buildEvent(Booking booking, String eventType) {
        return BookingEvent.builder()
                .eventType(eventType)
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .movieTitle(booking.getMovieTitle())
                .theatreName(booking.getTheatreName())
                .screenName(booking.getScreenName())
                .showDate(booking.getShowDate())
                .showTime(booking.getShowTime())
                .language(booking.getLanguage())
                .format(booking.getFormat())
                .seatNumbers(booking.getTickets().stream()
                        .map(Ticket::getSeatNumber)
                        .collect(Collectors.toList()))
                .totalAmount(booking.getTotalAmount())
                .totalSeats(booking.getTotalSeats())
                .status(booking.getStatus())
                .cancellationReason(booking.getCancellationReason())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}