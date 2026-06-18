package com.showtime.booking_service.constants;

/**
 * Application-wide constants for Booking Service.
 */
public final class AppConstants {

    private AppConstants() {}

    // ── Booking Status ──
    public static final String BOOKING_STATUS_PENDING   = "PENDING";
    public static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    public static final String BOOKING_STATUS_CANCELLED = "CANCELLED";
    public static final String BOOKING_STATUS_EXPIRED   = "EXPIRED";
    public static final String BOOKING_STATUS_REFUNDED  = "REFUNDED";

    // ── Ticket Status ──
    public static final String TICKET_STATUS_BOOKED    = "BOOKED";
    public static final String TICKET_STATUS_CANCELLED = "CANCELLED";
    public static final String TICKET_STATUS_USED      = "USED";

    // ── Seat Types ──
    public static final String SEAT_TYPE_STANDARD = "STANDARD";
    public static final String SEAT_TYPE_PREMIUM  = "PREMIUM";
    public static final String SEAT_TYPE_RECLINER = "RECLINER";

    // ── Redis Key Patterns ──
    // seat:{showId}:{seatNumber} → "{userId}:{bookingId}"
    public static final String SEAT_LOCK_KEY_PREFIX = "seat:";

    // ── RabbitMQ ──
    public static final String EXCHANGE_NAME              = "showtime.exchange";
    public static final String BOOKING_CONFIRMED_ROUTING  = "booking.confirmed";
    public static final String BOOKING_CANCELLED_ROUTING  = "booking.cancelled";
    public static final String PAYMENT_SUCCESS_ROUTING    = "payment.success";

    // ── Header Names (set by API Gateway) ──
    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    // ── Roles ──
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER  = "ROLE_USER";

    // ── Booking constraints ──
    public static final int MAX_SEATS_PER_BOOKING = 10;
}