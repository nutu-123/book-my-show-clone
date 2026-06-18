package com.showtime.booking_service.controller;


import com.showtime.booking_service.constants.AppConstants;
import com.showtime.booking_service.dto.request.CreateBookingRequest;
import com.showtime.booking_service.dto.request.LockSeatsRequest;
import com.showtime.booking_service.dto.response.ApiResponse;
import com.showtime.booking_service.dto.response.BookingResponse;
import com.showtime.booking_service.dto.response.BookingSummaryResponse;
import com.showtime.booking_service.dto.response.SeatLockResponse;
import com.showtime.booking_service.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Booking REST controller.
 *
 * All endpoints require JWT (set by API Gateway):
 *   Header: X-User-Id, X-User-Email, X-User-Roles
 *
 * Endpoints:
 *   POST /api/bookings/lock-seats     — Step 1: Lock seats
 *   POST /api/bookings                — Step 2: Create booking
 *   GET  /api/bookings/{id}           — Get booking detail
 *   GET  /api/bookings/ref/{ref}      — Get by reference
 *   GET  /api/bookings/user           — My bookings
 *   PUT  /api/bookings/{id}/cancel    — Cancel booking
 *   PUT  /api/bookings/{id}/confirm   — Confirm (internal/admin)
 *   GET  /api/bookings/admin/all      — All bookings (admin)
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log =
            LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    // ─────────────────────────────────────────────────────────
    // STEP 1: LOCK SEATS
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/bookings/lock-seats
     *
     * Lock selected seats in Redis (10 min TTL).
     * Returns price breakdown and lock confirmation.
     *
     * Body: { showId, seatNumbers }
     */
    @PostMapping("/lock-seats")
    public ResponseEntity<ApiResponse<SeatLockResponse>> lockSeats(
            @Valid @RequestBody LockSeatsRequest request,
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId) {

        log.info("Lock seats: show={}, user={}", request.getShowId(), userId);

        SeatLockResponse lockResponse =
                bookingService.lockSeats(request, Long.parseLong(userId));

        return ResponseEntity.ok(
                ApiResponse.success(lockResponse,
                        "Seats locked. Complete payment within 10 minutes."));
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2: CREATE BOOKING
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/bookings
     *
     * Create a PENDING booking (after seats are locked).
     * Returns bookingId for payment initiation.
     *
     * Body: { showId, seatNumbers, movieTitle, theatreName, ... }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(AppConstants.HEADER_USER_ID)    String userId,
            @RequestHeader(AppConstants.HEADER_USER_EMAIL) String userEmail) {

        log.info("Create booking: show={}, user={}", request.getShowId(), userId);

        BookingResponse booking = bookingService.createBooking(
                request, Long.parseLong(userId), userEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking,
                        "Booking created. Proceed to payment."));
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/bookings/{id}
     * Get full booking detail.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            @RequestHeader(AppConstants.HEADER_USER_ID)    String userId,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        boolean isAdmin = roles.contains(AppConstants.ROLE_ADMIN);
        BookingResponse booking = bookingService.getBookingById(
                id, Long.parseLong(userId), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * GET /api/bookings/ref/{reference}
     * Get booking by booking reference number.
     * Used for booking confirmation page.
     */
    @GetMapping("/ref/{reference}")
    public ResponseEntity<ApiResponse<BookingResponse>> getByReference(
            @PathVariable String reference,
            @RequestHeader(AppConstants.HEADER_USER_ID)    String userId,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        boolean isAdmin = roles.contains(AppConstants.ROLE_ADMIN);
        BookingResponse booking = bookingService.getBookingByReference(
                reference, Long.parseLong(userId), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * GET /api/bookings/user
     * Get all bookings for the logged-in user.
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<BookingSummaryResponse>>>
            getUserBookings(
                @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "10") int size) {

        Page<BookingSummaryResponse> bookings =
                bookingService.getUserBookings(
                        Long.parseLong(userId), page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<BookingSummaryResponse>>builder()
                        .success(true)
                        .message("Bookings fetched")
                        .data(bookings)
                        .page(bookings.getNumber())
                        .size(bookings.getSize())
                        .totalElements(bookings.getTotalElements())
                        .totalPages(bookings.getTotalPages())
                        .build());
    }

    // ─────────────────────────────────────────────────────────
    // CANCEL
    // ─────────────────────────────────────────────────────────

    /**
     * PUT /api/bookings/{id}/cancel
     * Cancel a booking.
     *
     * Param: reason (optional)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader(AppConstants.HEADER_USER_ID)    String userId,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        boolean isAdmin = roles.contains(AppConstants.ROLE_ADMIN);
        BookingResponse cancelled = bookingService.cancelBooking(
                id, Long.parseLong(userId), reason, isAdmin);

        return ResponseEntity.ok(
                ApiResponse.success(cancelled,
                        "Booking cancelled successfully. "
                        + "Refund will be processed in 5-7 business days."));
    }

    // ─────────────────────────────────────────────────────────
    // CONFIRM (internal — called by Payment Service)
    // ─────────────────────────────────────────────────────────

    /**
     * PUT /api/bookings/{id}/confirm
     * Confirm a booking after payment success.
     *
     * Called by Payment Service — NOT exposed to frontend directly.
     * Param: paymentId
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long id,
            @RequestParam Long paymentId) {

        BookingResponse confirmed =
                bookingService.confirmBooking(id, paymentId);
        return ResponseEntity.ok(
                ApiResponse.success(confirmed,
                        "Booking confirmed! Enjoy the show!"));
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/bookings/admin/all
     * All bookings paginated (admin only).
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<Page<BookingSummaryResponse>>>
            getAllBookings(
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size,
                @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                               defaultValue = "") String roles) {

        if (!roles.contains(AppConstants.ROLE_ADMIN)) {
            throw new com.showtime.booking_service.exception
                    .BookingException("Admin role required.");
        }

        Page<BookingSummaryResponse> bookings =
                bookingService.getAllBookings(page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<BookingSummaryResponse>>builder()
                        .success(true).message("All bookings fetched")
                        .data(bookings)
                        .page(bookings.getNumber())
                        .size(bookings.getSize())
                        .totalElements(bookings.getTotalElements())
                        .totalPages(bookings.getTotalPages())
                        .build());
    }
}