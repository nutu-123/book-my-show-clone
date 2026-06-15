package com.showtime.show_service.repository;

import com.showtime.show_service.document.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for Show documents.
 */
@Repository
public interface ShowRepository extends MongoRepository<Show, String> {

    // ── Core booking query: movie + city + date ──
    List<Show> findByMovieIdAndCityIgnoreCaseAndShowDateAndIsActiveTrue(
            String movieId, String city, LocalDate showDate);

    // ── All shows for a movie on a date ──
    List<Show> findByMovieIdAndShowDateAndIsActiveTrue(
            String movieId, LocalDate showDate);

    // ── Shows at a specific theatre ──
    List<Show> findByTheatreIdAndShowDateAndIsActiveTrue(
            String theatreId, LocalDate showDate);

    // ── Shows on a screen (admin/conflict check) ──
    List<Show> findByScreenIdAndShowDateAndIsActiveTrue(
            String screenId, LocalDate showDate);

    // ── Find conflicting show (same screen + date + time overlap) ──
    @Query("{ 'screen_id': ?0, 'show_date': ?1, 'is_active': true, " +
           "$or: [ " +
           "  { 'start_time': { $lt: ?3 }, 'end_time': { $gt: ?2 } } " +
           "] }")
    List<Show> findConflictingShows(
            String screenId, LocalDate showDate,
            LocalTime newStartTime, LocalTime newEndTime);

    // ── Upcoming shows for a movie ──
    List<Show> findByMovieIdAndShowDateGreaterThanEqualAndIsActiveTrue(
            String movieId, LocalDate fromDate);

    // ── Shows by theatre + date range ──
    Page<Show> findByTheatreIdAndShowDateBetweenAndIsActiveTrue(
            String theatreId, LocalDate from, LocalDate to, Pageable pageable);

    // ── Admin: all shows paginated ──
    Page<Show> findByIsActiveTrue(Pageable pageable);

    // ── Check duplicate (compound index backup) ──
    boolean existsByScreenIdAndShowDateAndStartTimeAndIsActiveTrue(
            String screenId, LocalDate showDate, LocalTime startTime);

    // ── Shows with available seats ──
    @Query("{ 'movie_id': ?0, 'city': { $regex: ?1, $options: 'i' }, " +
           "'show_date': ?2, 'is_active': true, 'available_seats': { $gt: 0 } }")
    List<Show> findAvailableShows(
            String movieId, String city, LocalDate showDate);

    // ── Count shows by status ──
    long countByStatusAndIsActiveTrue(String status);
}