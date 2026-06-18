package com.showtime.booking_service.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking entity — one booking per user per show transaction.
 * Contains multiple tickets (one per seat).
 *
 * State machine:
 *  PENDING → CONFIRMED (after payment success)
 *  PENDING → EXPIRED   (after 15 min, no payment)
 *  CONFIRMED → CANCELLED (user cancels, refund issued)
 *  CONFIRMED → REFUNDED  (after refund processed)
 */
@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_booking_user_id",  columnList = "user_id"),
        @Index(name = "idx_booking_show_id",  columnList = "show_id"),
        @Index(name = "idx_booking_status",   columnList = "status"),
        @Index(name = "idx_booking_ref",      columnList = "booking_reference",
               unique = true)
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", unique = true,
            nullable = false, length = 20)
    private String bookingReference;      // e.g., "ST20241225001234"

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "show_id", nullable = false, length = 100)
    private String showId;

    @Column(name = "movie_id", length = 100)
    private String movieId;

    @Column(name = "movie_title", length = 200)
    private String movieTitle;

    @Column(name = "theatre_id", length = 100)
    private String theatreId;

    @Column(name = "theatre_name", length = 200)
    private String theatreName;

    @Column(name = "screen_id", length = 100)
    private String screenId;

    @Column(name = "screen_name", length = 100)
    private String screenName;

    @Column(name = "show_date", length = 20)
    private String showDate;           // Stored as string for simplicity

    @Column(name = "show_time", length = 20)
    private String showTime;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "format", length = 20)
    private String format;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "convenience_fee", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal convenienceFee = BigDecimal.ZERO;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One booking → many tickets (one per seat)
    @OneToMany(mappedBy = "booking",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt   = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
        bookingTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Helper: add a ticket ──
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setBooking(this);
    }
}