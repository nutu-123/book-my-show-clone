package com.showtime.booking_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ticket entity — one ticket per seat per booking.
 *
 * Each ticket represents one physical seat in a show.
 * Tickets are generated when booking is CONFIRMED.
 */
@Entity
@Table(
    name = "tickets",
    indexes = {
        @Index(name = "idx_ticket_booking_id",   columnList = "booking_id"),
        @Index(name = "idx_ticket_show_seat",
               columnList = "show_id, seat_number")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "booking")   // Avoid circular toString
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", unique = true,
            nullable = false, length = 30)
    private String ticketNumber;       // e.g., "TKT20241225001234-A1"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "show_id", nullable = false, length = 100)
    private String showId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;         // e.g., "A1", "B12", "J3"

    @Column(name = "seat_type", length = 20)
    private String seatType;           // STANDARD, PREMIUM, RECLINER

    @Column(name = "row_label", length = 5)
    private String rowLabel;           // e.g., "A", "B", "J"

    @Column(name = "column_number")
    private Integer columnNumber;      // e.g., 1, 2, 12

    @Column(name = "price", precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "BOOKED";  // BOOKED, CANCELLED, USED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}