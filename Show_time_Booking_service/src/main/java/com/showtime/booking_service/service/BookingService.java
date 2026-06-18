package com.showtime.booking_service.service;

import com.showtime.booking_service.client.ShowServiceClient;
import com.showtime.booking_service.constants.AppConstants;
import com.showtime.booking_service.dto.request.CreateBookingRequest;
import com.showtime.booking_service.dto.request.LockSeatsRequest;
import com.showtime.booking_service.dto.response.BookingResponse;
import com.showtime.booking_service.dto.response.BookingSummaryResponse;
import com.showtime.booking_service.dto.response.SeatLockResponse;
import com.showtime.booking_service.dto.response.TicketResponse;
import com.showtime.booking_service.entity.Booking;
import com.showtime.booking_service.entity.Ticket;
import com.showtime.booking_service.exception.BookingException;
import com.showtime.booking_service.exception.ResourceNotFoundException;
import com.showtime.booking_service.repository.BookingRepository;
import com.showtime.booking_service.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core booking orchestration service.
 *
 * ═══════════════════════════════════════════════
 * BOOKING FLOW
 * ═══════════════════════════════════════════════
 *
 * Step 1: POST /api/bookings/lock-seats
 *   → Lock seats in Redis (TTL=10min)
 *   → Return price calculation
 *
 * Step 2: POST /api/bookings
 *   → Verify locks still held
 *   → Create PENDING booking in DB
 *   → Return bookingId for payment
 *
 * Step 3: Payment Service initiates payment
 *
 * Step 4: Payment Service publishes payment.success event
 *
 * Step 5: BookingService consumes payment.success
 *   → confirmBooking() called
 *   → Status: PENDING → CONFIRMED
 *   → Generate tickets
 *   → Update Show Service (seats marked booked)
 *   → Release Redis locks
 *   → Publish booking.confirmed event
 *   → Notification Service sends email/SMS
 *
 * ═══════════════════════════════════════════════
 */
@Service
public class BookingService {

    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);

    private static final BigDecimal CONVENIENCE_FEE_PERCENT =
            new BigDecimal("0.02");   // 2% convenience fee

    @Autowired private BookingRepository      bookingRepository;
    @Autowired private TicketRepository       ticketRepository;
    @Autowired private SeatLockService        seatLockService;
    @Autowired private BookingEventPublisher  eventPublisher;
    @Autowired private ShowServiceClient      showServiceClient;

    @Value("${booking.seat-lock-ttl-seconds:600}")
    private long seatLockTtl;

    @Value("${booking.booking-expiry-minutes:15}")
    private int bookingExpiryMinutes;

    // ─────────────────────────────────────────────────────────
    // STEP 1: LOCK SEATS
    // ─────────────────────────────────────────────────────────

    /**
     * Lock seats in Redis and return price calculation.
     *
     * This is ATOMIC — either all seats lock or none do.
     * If any seat is taken, all acquired locks are rolled back.
     */
    public SeatLockResponse lockSeats(LockSeatsRequest request,
                                       Long userId) {
        log.info("Lock seats request: show={}, seats={}, user={}",
                request.getShowId(), request.getSeatNumbers(), userId);

        // ── Validate seat count ──
        if (request.getSeatNumbers().size() > AppConstants.MAX_SEATS_PER_BOOKING) {
            throw new BookingException(
                    "Maximum " + AppConstants.MAX_SEATS_PER_BOOKING
                    + " seats allowed per booking.");
        }

        // ── Attempt atomic lock via Redis ──
        seatLockService.lockSeats(
                request.getShowId(),
                request.getSeatNumbers(),
                userId
        );

        // ── Calculate prices ──
        // In production: fetch seat prices from Show Service
        // Here we use hardcoded prices by seat type for simplicity
        List<SeatLockResponse.SeatDetail> seatDetails =
                buildSeatDetails(request.getSeatNumbers());

        BigDecimal totalAmount = seatDetails.stream()
                .map(SeatLockResponse.SeatDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal convenienceFee = totalAmount
                .multiply(CONVENIENCE_FEE_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = totalAmount.add(convenienceFee);

        LocalDateTime lockExpiresAt =
                LocalDateTime.now().plusSeconds(seatLockTtl);

        log.info("Seats locked: {} for show {} — total: {}",
                request.getSeatNumbers(), request.getShowId(), grandTotal);

        return SeatLockResponse.builder()
                .showId(request.getShowId())
                .lockedSeats(request.getSeatNumbers())
                .lockTtlSeconds((int) seatLockTtl)
                .totalAmount(totalAmount)
                .convenienceFee(convenienceFee)
                .grandTotal(grandTotal)
                .seatDetails(seatDetails)
                .lockExpiresAt(lockExpiresAt.toString())
                .message("Seats locked for 10 minutes. Please complete payment.")
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2: CREATE BOOKING
    // ─────────────────────────────────────────────────────────

    /**
     * Create a PENDING booking.
     *
     * Verifies that the user still holds Redis locks before
     * creating the DB record. If locks have expired, throws exception.
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request,
                                          Long userId,
                                          String userEmail) {
        log.info("Creating booking: show={}, seats={}, user={}",
                request.getShowId(), request.getSeatNumbers(), userId);

        // ── Validate seat count ──
        if (request.getSeatNumbers().size() > AppConstants.MAX_SEATS_PER_BOOKING) {
            throw new BookingException("Maximum 10 seats per booking.");
        }

        // ── Verify user still holds locks ──
        List<String> invalidLocks = seatLockService
                .getExpiredOrStolenLocks(
                        request.getShowId(),
                        request.getSeatNumbers(),
                        userId
                );

        if (!invalidLocks.isEmpty()) {
            throw new BookingException(
                    "Seat lock expired or invalid for seats: " + invalidLocks
                    + ". Please re-select seats and try again.");
        }

        // ── Calculate amounts ──
        List<SeatLockResponse.SeatDetail> seatDetails =
                buildSeatDetails(request.getSeatNumbers());

        BigDecimal totalAmount = seatDetails.stream()
                .map(SeatLockResponse.SeatDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal convenienceFee = totalAmount
                .multiply(CONVENIENCE_FEE_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = totalAmount.add(convenienceFee);

        // ── Build booking entity ──
        String bookingRef = generateBookingReference();

        Booking booking = Booking.builder()
                .bookingReference(bookingRef)
                .userId(userId)
                .userEmail(userEmail)
                .showId(request.getShowId())
                .movieId(request.getMovieId())
                .movieTitle(request.getMovieTitle())
                .theatreId(request.getTheatreId())
                .theatreName(request.getTheatreName())
                .screenId(request.getScreenId())
                .screenName(request.getScreenName())
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .language(request.getLanguage())
                .format(request.getFormat())
                .totalAmount(totalAmount)
                .convenienceFee(convenienceFee)
                .totalSeats(request.getSeatNumbers().size())
                .status(AppConstants.BOOKING_STATUS_PENDING)
                .expiryTime(LocalDateTime.now()
                        .plusMinutes(bookingExpiryMinutes))
                .build();

        // ── Save booking first to get ID ──
        Booking savedBooking = bookingRepository.save(booking);

        // ── Create ticket stubs (PENDING, no ticket number yet) ──
        for (int i = 0; i < request.getSeatNumbers().size(); i++) {
            String seatNumber = request.getSeatNumbers().get(i);
            SeatLockResponse.SeatDetail detail = seatDetails.get(i);

            Ticket ticket = Ticket.builder()
                    .ticketNumber(generateTicketNumber(bookingRef, seatNumber))
                    .showId(request.getShowId())
                    .seatNumber(seatNumber)
                    .seatType(detail.getSeatType())
                    .rowLabel(seatNumber.replaceAll("[0-9]", ""))
                    .columnNumber(extractColumnNumber(seatNumber))
                    .price(detail.getPrice())
                    .status(AppConstants.TICKET_STATUS_BOOKED)
                    .build();

            savedBooking.addTicket(ticket);
        }

        savedBooking = bookingRepository.save(savedBooking);

        log.info("Booking created: {} (id={}) — status: PENDING",
                bookingRef, savedBooking.getId());

        return mapToResponse(savedBooking);
    }

    // ─────────────────────────────────────────────────────────
    // STEP 5: CONFIRM BOOKING (after payment success)
    // ─────────────────────────────────────────────────────────

    /**
     * Confirm a booking after payment success.
     *
     * Called by:
     *  - Payment Service via RabbitMQ event (production)
     *  - Payment Service via direct REST call (simpler for dev)
     *
     * Steps:
     *  1. Verify booking is still PENDING
     *  2. Update status → CONFIRMED
     *  3. Update Show Service (mark seats as booked)
     *  4. Release Redis seat locks
     *  5. Publish booking.confirmed event → Notification Service
     */
    @Transactional
    public BookingResponse confirmBooking(Long bookingId, Long paymentId) {
        log.info("Confirming booking: {} with payment: {}",
                bookingId, paymentId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + bookingId));

        // ── Validate status ──
        if (!AppConstants.BOOKING_STATUS_PENDING.equals(booking.getStatus())) {
            throw new BookingException(
                    "Booking cannot be confirmed. Status: "
                    + booking.getStatus());
        }

        // ── Check not expired ──
        if (LocalDateTime.now().isAfter(booking.getExpiryTime())) {
            booking.setStatus(AppConstants.BOOKING_STATUS_EXPIRED);
            bookingRepository.save(booking);
            throw new BookingException(
                    "Booking has expired. Please start a new booking.");
        }

        // ── Update booking status ──
        booking.setStatus(AppConstants.BOOKING_STATUS_CONFIRMED);
        booking.setPaymentId(paymentId);
        booking.setConfirmedTime(LocalDateTime.now());
        Booking confirmedBooking = bookingRepository.save(booking);

        // ── Get seat numbers ──
        List<String> seatNumbers = confirmedBooking.getTickets()
                .stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toList());

        // ── Update Show Service ──
        try {
            showServiceClient.updateSeatAvailability(
                    booking.getShowId(), seatNumbers, true);
            log.info("Show Service updated: {} seats marked booked",
                    seatNumbers.size());
        } catch (Exception e) {
            // Log but don't fail — fallback handles this
            log.error("Failed to update Show Service: {}", e.getMessage());
        }

        // ── Release Redis locks ──
        seatLockService.forceReleaseSeats(
                booking.getShowId(), seatNumbers);

        // ── Publish booking confirmed event ──
        eventPublisher.publishBookingConfirmed(confirmedBooking);

        log.info("Booking confirmed: {} — {} seats",
                booking.getBookingReference(), seatNumbers.size());

        return mapToResponse(confirmedBooking);
    }

    // ─────────────────────────────────────────────────────────
    // CANCEL BOOKING
    // ─────────────────────────────────────────────────────────

    /**
     * Cancel a confirmed booking.
     *
     * Rules:
     *  - User can only cancel their own booking
     *  - Admin can cancel any booking
     *  - Cancellation triggers refund (handled by Payment Service)
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId,
                                          Long userId,
                                          String reason,
                                          boolean isAdmin) {
        log.info("Cancellation request: booking={}, user={}",
                bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + bookingId));

        // ── Ownership check ──
        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new BookingException(
                    "Unauthorized: Cannot cancel another user's booking.");
        }

        // ── Validate cancellable status ──
        if (!AppConstants.BOOKING_STATUS_CONFIRMED.equals(booking.getStatus())
                && !AppConstants.BOOKING_STATUS_PENDING.equals(booking.getStatus())) {
            throw new BookingException(
                    "Booking cannot be cancelled. Current status: "
                    + booking.getStatus());
        }

        // ── Cancel tickets ──
        ticketRepository.cancelTicketsByBookingId(bookingId);

        // ── Update booking ──
        booking.setStatus(AppConstants.BOOKING_STATUS_CANCELLED);
        booking.setCancelledTime(LocalDateTime.now());
        booking.setCancellationReason(
                reason != null ? reason : "Cancelled by user");
        Booking cancelled = bookingRepository.save(booking);

        // ── Release seats in Show Service ──
        List<String> seatNumbers = cancelled.getTickets()
                .stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toList());

        try {
            showServiceClient.updateSeatAvailability(
                    booking.getShowId(), seatNumbers, false);
        } catch (Exception e) {
            log.error("Failed to release seats in Show Service: {}",
                    e.getMessage());
        }

        // ── Release Redis locks (if still held) ──
        seatLockService.forceReleaseSeats(booking.getShowId(), seatNumbers);

        // ── Publish cancellation event ──
        eventPublisher.publishBookingCancelled(cancelled);

        log.info("Booking cancelled: {}", booking.getBookingReference());
        return mapToResponse(cancelled);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    /**
     * Get full booking detail by ID.
     * User can only access their own bookings.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId,
                                           Long userId,
                                           boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + bookingId));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new BookingException(
                    "Unauthorized: Cannot access this booking.");
        }

        return mapToResponse(booking);
    }

    /**
     * Get booking by reference number.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference,
                                                   Long userId,
                                                   boolean isAdmin) {
        Booking booking = bookingRepository
                .findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + reference));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new BookingException("Unauthorized.");
        }

        return mapToResponse(booking);
    }

    /**
     * Get all bookings for the logged-in user (paginated).
     */
    @Transactional(readOnly = true)
    public Page<BookingSummaryResponse> getUserBookings(Long userId,
                                                         int page,
                                                         int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "bookingTime"));
        return bookingRepository
                .findByUserIdOrderByBookingTimeDesc(userId, pageable)
                .map(this::mapToSummary);
    }

    /**
     * Get all bookings (admin only, paginated).
     */
    @Transactional(readOnly = true)
    public Page<BookingSummaryResponse> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "bookingTime"));
        return bookingRepository
                .findAllByOrderByBookingTimeDesc(pageable)
                .map(this::mapToSummary);
    }

    // ─────────────────────────────────────────────────────────
    // SCHEDULED: EXPIRE PENDING BOOKINGS
    // ─────────────────────────────────────────────────────────

    /**
     * Runs every 5 minutes.
     * Expires PENDING bookings past their expiry time.
     * Releases their seat locks and updates Show Service.
     */
    @Scheduled(fixedDelay = 300000)   // Every 5 minutes
    @Transactional
    public void expirePendingBookings() {
        log.debug("Running pending booking expiry job...");

        List<Booking> expiredBookings = bookingRepository
                .findExpiredPendingBookings(LocalDateTime.now());

        if (expiredBookings.isEmpty()) {
            log.debug("No expired bookings found.");
            return;
        }

        log.info("Expiring {} pending bookings...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                booking.setStatus(AppConstants.BOOKING_STATUS_EXPIRED);
                bookingRepository.save(booking);

                // Release seats
                List<String> seatNumbers = booking.getTickets()
                        .stream()
                        .map(Ticket::getSeatNumber)
                        .collect(Collectors.toList());

                if (!seatNumbers.isEmpty()) {
                    seatLockService.forceReleaseSeats(
                            booking.getShowId(), seatNumbers);
                }

                log.info("Booking expired: {}", booking.getBookingReference());
            } catch (Exception e) {
                log.error("Error expiring booking {}: {}",
                        booking.getBookingReference(), e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Generate a unique booking reference.
     * Format: ST{yyyyMMdd}{6-digit-random}
     * Example: ST202412250012345
     */
    private String generateBookingReference() {
        String datePart = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d",
                (int)(Math.random() * 1_000_000));
        return "ST" + datePart + randomPart;
    }

    /**
     * Generate a ticket number.
     * Format: TKT-{bookingRef}-{seatNumber}
     * Example: TKT-ST20241225001234-A1
     */
    private String generateTicketNumber(String bookingRef,
                                         String seatNumber) {
        return "TKT-" + bookingRef + "-" + seatNumber;
    }

    /**
     * Extract column number from seat string.
     * "A1" → 1, "B12" → 12
     */
    private Integer extractColumnNumber(String seatNumber) {
        try {
            String numPart = seatNumber.replaceAll("[^0-9]", "");
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Build seat details with type and price.
     * Row A-E = STANDARD @ 180
     * Row F-H = PREMIUM  @ 250
     * Row I-J = RECLINER @ 400
     *
     * NOTE: In production, fetch actual prices from Show Service.
     * This is a simplified price lookup.
     */
    private List<SeatLockResponse.SeatDetail> buildSeatDetails(
            List<String> seatNumbers) {

        List<SeatLockResponse.SeatDetail> details = new ArrayList<>();

        for (String seatNumber : seatNumbers) {
            String row = seatNumber.replaceAll("[0-9]", "").toUpperCase();
            String seatType;
            BigDecimal price;

            if (List.of("A","B","C","D","E").contains(row)) {
                seatType = AppConstants.SEAT_TYPE_STANDARD;
                price    = new BigDecimal("180.00");
            } else if (List.of("F","G","H").contains(row)) {
                seatType = AppConstants.SEAT_TYPE_PREMIUM;
                price    = new BigDecimal("250.00");
            } else if (List.of("I","J").contains(row)) {
                seatType = AppConstants.SEAT_TYPE_RECLINER;
                price    = new BigDecimal("400.00");
            } else {
                seatType = AppConstants.SEAT_TYPE_STANDARD;
                price    = new BigDecimal("200.00");
            }

            details.add(SeatLockResponse.SeatDetail.builder()
                    .seatNumber(seatNumber)
                    .seatType(seatType)
                    .price(price)
                    .build());
        }

        return details;
    }

    // ─────────────────────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────────────────────

    public BookingResponse mapToResponse(Booking booking) {
        List<TicketResponse> tickets = booking.getTickets() == null
                ? List.of()
                : booking.getTickets().stream()
                        .map(t -> TicketResponse.builder()
                                .id(t.getId())
                                .ticketNumber(t.getTicketNumber())
                                .seatNumber(t.getSeatNumber())
                                .seatType(t.getSeatType())
                                .rowLabel(t.getRowLabel())
                                .columnNumber(t.getColumnNumber())
                                .price(t.getPrice())
                                .status(t.getStatus())
                                .build())
                        .collect(Collectors.toList());

        List<String> seatNumbers = tickets.stream()
                .map(TicketResponse::getSeatNumber)
                .collect(Collectors.toList());

        BigDecimal grandTotal = booking.getTotalAmount() != null
                ? booking.getTotalAmount()
                        .add(booking.getConvenienceFee() != null
                                ? booking.getConvenienceFee()
                                : BigDecimal.ZERO)
                : BigDecimal.ZERO;

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .showId(booking.getShowId())
                .movieId(booking.getMovieId())
                .movieTitle(booking.getMovieTitle())
                .theatreId(booking.getTheatreId())
                .theatreName(booking.getTheatreName())
                .screenId(booking.getScreenId())
                .screenName(booking.getScreenName())
                .showDate(booking.getShowDate())
                .showTime(booking.getShowTime())
                .language(booking.getLanguage())
                .format(booking.getFormat())
                .totalAmount(booking.getTotalAmount())
                .convenienceFee(booking.getConvenienceFee())
                .grandTotal(grandTotal)
                .totalSeats(booking.getTotalSeats())
                .status(booking.getStatus())
                .paymentId(booking.getPaymentId())
                .tickets(tickets)
                .seatNumbers(seatNumbers)
                .bookingTime(booking.getBookingTime())
                .expiryTime(booking.getExpiryTime())
                .confirmedTime(booking.getConfirmedTime())
                .cancelledTime(booking.getCancelledTime())
                .cancellationReason(booking.getCancellationReason())
                .build();
    }

    public BookingSummaryResponse mapToSummary(Booking booking) {
        List<String> seatNumbers = booking.getTickets() == null
                ? List.of()
                : booking.getTickets().stream()
                        .map(Ticket::getSeatNumber)
                        .collect(Collectors.toList());

        BigDecimal grandTotal = booking.getTotalAmount() != null
                ? booking.getTotalAmount()
                        .add(booking.getConvenienceFee() != null
                                ? booking.getConvenienceFee()
                                : BigDecimal.ZERO)
                : BigDecimal.ZERO;

        return BookingSummaryResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .movieTitle(booking.getMovieTitle())
                .theatreName(booking.getTheatreName())
                .screenName(booking.getScreenName())
                .showDate(booking.getShowDate())
                .showTime(booking.getShowTime())
                .language(booking.getLanguage())
                .format(booking.getFormat())
                .seatNumbers(seatNumbers)
                .totalSeats(booking.getTotalSeats())
                .grandTotal(grandTotal)
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .confirmedTime(booking.getConfirmedTime())
                .build();
    }
}