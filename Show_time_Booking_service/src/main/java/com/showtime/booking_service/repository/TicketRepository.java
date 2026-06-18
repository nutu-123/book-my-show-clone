package com.showtime.booking_service.repository;


import com.showtime.booking_service.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Ticket entity.
 */
@Repository
public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    // ── Tickets by booking ──
    List<Ticket> findByBookingId(Long bookingId);

    // ── Find by ticket number ──
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    // ── Tickets for a show ──
    List<Ticket> findByShowIdAndStatus(String showId, String status);

    // ── Check specific seat in a show ──
    boolean existsByShowIdAndSeatNumberAndStatus(
            String showId, String seatNumber, String status);

    // ── Cancel all tickets in a booking ──
    @Modifying
    @Query("UPDATE Ticket t SET t.status = 'CANCELLED' " +
           "WHERE t.booking.id = :bookingId")
    void cancelTicketsByBookingId(@Param("bookingId") Long bookingId);

    // ── Get seat numbers for a show (booked) ──
    @Query("SELECT t.seatNumber FROM Ticket t " +
           "WHERE t.showId = :showId AND t.status = 'BOOKED'")
    List<String> findBookedSeatNumbers(@Param("showId") String showId);
}