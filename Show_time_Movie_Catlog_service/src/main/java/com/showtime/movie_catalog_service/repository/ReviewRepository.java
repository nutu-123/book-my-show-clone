package com.showtime.movie_catalog_service.repository;

import com.showtime.movie_catalog_service.document.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review MongoDB documents.
 */
@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // ── Get all reviews for a movie ──
    Page<Review> findByMovieId(String movieId, Pageable pageable);

    // ── Get a specific user's review for a movie ──
    Optional<Review> findByMovieIdAndUserId(String movieId, String userId);

    // ── Check if user already reviewed ──
    boolean existsByMovieIdAndUserId(String movieId, String userId);

    // ── Delete all reviews for a movie ──
    void deleteByMovieId(String movieId);

    // ── Count reviews for a movie ──
    long countByMovieId(String movieId);

    // ── Get all reviews by a user ──
    List<Review> findByUserId(String userId);

    // ── Average rating for a movie ──
    @Query(value  = "{ 'movie_id': ?0 }",
           fields = "{ 'rating': 1 }")
    List<Review> findRatingsByMovieId(String movieId);
}