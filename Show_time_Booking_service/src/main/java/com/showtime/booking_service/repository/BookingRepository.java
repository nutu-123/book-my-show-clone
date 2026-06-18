package com.showtime.booking_service.repository;


import com.showtime.booking_service.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking entity.
 */
@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    // ── User bookings ──
    Page<Booking> findByUserIdOrderByBookingTimeDesc(
            Long userId, Pageable pageable);

    List<Booking> findByUserIdAndStatusOrderByBookingTimeDesc(
            Long userId, String status);

    // ── Find by reference ──
    Optional<Booking> findByBookingReference(String bookingReference);

    // ── Find PENDING bookings past expiry (for cleanup) ──
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
           "AND b.expiryTime < :now")
    List<Booking> findExpiredPendingBookings(
            @Param("now") LocalDateTime now);

    // ── Find by showId (admin) ──
    List<Booking> findByShowIdAndStatus(String showId, String status);

    // ── Count confirmed bookings for a show ──
    long countByShowIdAndStatus(String showId, String status);

    // ── Update status in bulk (for expiry job) ──
    @Modifying
    @Query("UPDATE Booking b SET b.status = 'EXPIRED', b.updatedAt = :now " +
           "WHERE b.status = 'PENDING' AND b.expiryTime < :now")
    int expirePendingBookings(@Param("now") LocalDateTime now);

    // ── Find by payment ID ──
    Optional<Booking> findByPaymentId(Long paymentId);

    // ── Admin: all bookings paginated ──
    Page<Booking> findAllByOrderByBookingTimeDesc(Pageable pageable);

    // ── Check if user already has active booking for a show ──
    boolean existsByUserIdAndShowIdAndStatusIn(
            Long userId, String showId, List<String> statuses);
}