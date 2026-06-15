package com.showtime.movie_catalog_service.repository;

import com.showtime.movie_catalog_service.document.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Movie MongoDB documents.
 *
 * Spring Data MongoDB auto-implements all these methods
 * based on method name conventions.
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    // ── Active movies ──

    Page<Movie> findByIsActiveTrue(Pageable pageable);

    Page<Movie> findByIsActiveTrueAndIsUpcomingFalse(Pageable pageable);

    // ── Search by title (case-insensitive partial match) ──

    Page<Movie> findByTitleContainingIgnoreCaseAndIsActiveTrue(
            String title, Pageable pageable);

    // ── Filter by genre ──

    Page<Movie> findByGenresContainingIgnoreCaseAndIsActiveTrue(
            String genre, Pageable pageable);

    // ── Filter by language ──

    Page<Movie> findByLanguagesContainingIgnoreCaseAndIsActiveTrue(
            String language, Pageable pageable);

    // ── Filter by genre and language ──

    Page<Movie> findByGenresContainingIgnoreCaseAndLanguagesContainingIgnoreCaseAndIsActiveTrue(
            String genre, String language, Pageable pageable);

    // ── Upcoming movies ──

    Page<Movie> findByIsUpcomingTrueAndIsActiveTrue(Pageable pageable);

    // ── Currently running (released but not upcoming) ──

    Page<Movie> findByReleaseDateBeforeAndIsActiveTrueAndIsUpcomingFalse(
            LocalDate date, Pageable pageable);

    // ── Trending (order by trending score desc) ──

    @Query("{ 'is_active': true, 'is_upcoming': false }")
    List<Movie> findTop10ByIsActiveTrueOrderByTrendingScoreDesc(Pageable pageable);

    // ── Top rated ──

    Page<Movie> findByIsActiveTrueOrderByRatingDesc(Pageable pageable);

    // ── By release date range ──

    Page<Movie> findByReleaseDateBetweenAndIsActiveTrue(
            LocalDate from, LocalDate to, Pageable pageable);

    // ── Check duplicate title + release date ──

    boolean existsByTitleIgnoreCaseAndReleaseDate(
            String title, LocalDate releaseDate);

    // ── Get distinct genres ──

    @Query(value = "{}", fields = "{ 'genres': 1 }")
    List<Movie> findAllGenres();

    // ── Count active movies ──

    long countByIsActiveTrue();

    // ── Find by format ──

    Page<Movie> findByFormatsContainingIgnoreCaseAndIsActiveTrue(
            String format, Pageable pageable);
}