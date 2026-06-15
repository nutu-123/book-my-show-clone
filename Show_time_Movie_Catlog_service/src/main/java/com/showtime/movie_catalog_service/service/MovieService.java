package com.showtime.movie_catalog_service.service;


import com.showtime.movie_catalog_service.constants.AppConstants;
import com.showtime.movie_catalog_service.document.Movie;
import com.showtime.movie_catalog_service.dto.request.CreateMovieRequest;
import com.showtime.movie_catalog_service.dto.request.UpdateMovieRequest;
import com.showtime.movie_catalog_service.dto.response.MovieResponse;
import com.showtime.movie_catalog_service.dto.response.MovieSummaryResponse;
import com.showtime.movie_catalog_service.exception.ResourceNotFoundException;
import com.showtime.movie_catalog_service.repository.MovieRepository;
import com.showtime.movie_catalog_service.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core service for movie management.
 *
 * Handles all CRUD operations, search, filtering,
 * trending score calculation, and genre listing.
 */
@Service
public class MovieService {

    private static final Logger log =
            LoggerFactory.getLogger(MovieService.class);

    @Autowired private MovieRepository  movieRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private MongoTemplate    mongoTemplate;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    /**
     * Create a new movie (Admin only).
     */
    public MovieResponse createMovie(CreateMovieRequest request) {
        log.info("Creating movie: {}", request.getTitle());

        Movie movie = Movie.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .duration(request.getDuration())
                .languages(request.getLanguages())
                .releaseDate(request.getReleaseDate())
                .genres(request.getGenres())
                .cast(mapCastMembers(request.getCast()))
                .director(request.getDirector())
                .producer(request.getProducer())
                .posterUrl(request.getPosterUrl())
                .bannerUrl(request.getBannerUrl())
                .trailerUrl(request.getTrailerUrl())
                .certificate(request.getCertificate())
                .formats(request.getFormats() != null
                        ? request.getFormats()
                        : List.of("2D"))
                .isUpcoming(request.getIsUpcoming() != null
                        ? request.getIsUpcoming() : false)
                .isActive(true)
                .rating(0.0)
                .totalRatings(0)
                .trendingScore(0.0)
                .build();

        Movie saved = movieRepository.save(movie);
        log.info("Movie created: {} (id={})", saved.getTitle(), saved.getId());
        return mapToMovieResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // READ — SINGLE
    // ─────────────────────────────────────────────────────────

    /**
     * Get full movie details by ID.
     */
    public MovieResponse getMovieById(String movieId) {
        Movie movie = findMovieById(movieId);
        return mapToMovieResponse(movie);
    }

    // ─────────────────────────────────────────────────────────
    // READ — LISTINGS
    // ─────────────────────────────────────────────────────────

    /**
     * Get all active, currently showing movies (paginated).
     */
    public Page<MovieSummaryResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "releaseDate"));
        return movieRepository
                .findByIsActiveTrueAndIsUpcomingFalse(pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get upcoming movies.
     */
    public Page<MovieSummaryResponse> getUpcomingMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "releaseDate"));
        return movieRepository
                .findByIsUpcomingTrueAndIsActiveTrue(pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get trending movies (top 10 by trending score).
     */
    public List<MovieSummaryResponse> getTrendingMovies() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "trendingScore"));
        return movieRepository
                .findTop10ByIsActiveTrueOrderByTrendingScoreDesc(pageable)
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get top-rated movies.
     */
    public Page<MovieSummaryResponse> getTopRatedMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "rating"));
        return movieRepository
                .findByIsActiveTrueOrderByRatingDesc(pageable)
                .map(this::mapToSummaryResponse);
    }

    // ─────────────────────────────────────────────────────────
    // SEARCH & FILTER
    // ─────────────────────────────────────────────────────────

    /**
     * Search movies by title (partial, case-insensitive).
     */
    public Page<MovieSummaryResponse> searchMovies(
            String query, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "rating"));
        return movieRepository
                .findByTitleContainingIgnoreCaseAndIsActiveTrue(query, pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Advanced filter — genre, language, format (any combination).
     * Uses MongoTemplate for dynamic query building.
     */
    public Page<MovieSummaryResponse> filterMovies(
            String genre, String language, String format,
            String certificate, int page, int size) {

        Criteria criteria = Criteria.where("is_active").is(true)
                                    .and("is_upcoming").is(false);

        if (genre != null && !genre.isBlank()) {
            criteria = criteria.and("genres")
                    .regex(genre, "i");   // case-insensitive
        }
        if (language != null && !language.isBlank()) {
            criteria = criteria.and("languages")
                    .regex(language, "i");
        }
        if (format != null && !format.isBlank()) {
            criteria = criteria.and("formats")
                    .regex(format, "i");
        }
        if (certificate != null && !certificate.isBlank()) {
            criteria = criteria.and("certificate")
                    .regex(certificate, "i");
        }

        Query query = new Query(criteria)
                .with(PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "rating")));

        List<Movie> movies = mongoTemplate.find(query, Movie.class);

        long total = mongoTemplate.count(
                new Query(criteria), Movie.class);

        // Convert to Page manually
        List<MovieSummaryResponse> content = movies.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                content,
                PageRequest.of(page, size),
                total
        );
    }

    /**
     * Get all distinct genre names.
     */
    public List<String> getAllGenres() {
        return mongoTemplate.findDistinct(
                new Query(Criteria.where("is_active").is(true)),
                "genres",
                Movie.class,
                String.class
        );
    }

    /**
     * Get all distinct languages.
     */
    public List<String> getAllLanguages() {
        return mongoTemplate.findDistinct(
                new Query(Criteria.where("is_active").is(true)),
                "languages",
                Movie.class,
                String.class
        );
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────

    /**
     * Update a movie (Admin only).
     * Only non-null fields are updated.
     */
    public MovieResponse updateMovie(String movieId,
                                      UpdateMovieRequest request) {
        log.info("Updating movie: {}", movieId);

        Movie movie = findMovieById(movieId);

        if (request.getTitle()       != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDuration()    != null) movie.setDuration(request.getDuration());
        if (request.getLanguages()   != null) movie.setLanguages(request.getLanguages());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getGenres()      != null) movie.setGenres(request.getGenres());
        if (request.getDirector()    != null) movie.setDirector(request.getDirector());
        if (request.getProducer()    != null) movie.setProducer(request.getProducer());
        if (request.getPosterUrl()   != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getBannerUrl()   != null) movie.setBannerUrl(request.getBannerUrl());
        if (request.getTrailerUrl()  != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getCertificate() != null) movie.setCertificate(request.getCertificate());
        if (request.getFormats()     != null) movie.setFormats(request.getFormats());
        if (request.getIsActive()    != null) movie.setIsActive(request.getIsActive());
        if (request.getIsUpcoming()  != null) movie.setIsUpcoming(request.getIsUpcoming());
        if (request.getCast()        != null) {
            movie.setCast(mapCastMembers(request.getCast()));
        }

        Movie saved = movieRepository.save(movie);
        log.info("Movie updated: {} (id={})", saved.getTitle(), saved.getId());
        return mapToMovieResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────

    /**
     * Soft-delete a movie (sets isActive = false).
     * Hard delete is avoided to preserve booking history references.
     */
    public void deleteMovie(String movieId) {
        log.info("Soft-deleting movie: {}", movieId);
        Movie movie = findMovieById(movieId);
        movie.setIsActive(false);
        movieRepository.save(movie);
        log.info("Movie deactivated: {}", movieId);
    }

    // ─────────────────────────────────────────────────────────
    // RATING UPDATE (called by ReviewService)
    // ─────────────────────────────────────────────────────────

    /**
     * Recalculate and update movie's average rating.
     * Called after a review is created or deleted.
     */
    public void recalculateRating(String movieId) {
        List<com.showtime.movie_catalog_service.document.Review> reviews =
                reviewRepository.findRatingsByMovieId(movieId);

        if (reviews.isEmpty()) {
            Movie movie = findMovieById(movieId);
            movie.setRating(0.0);
            movie.setTotalRatings(0);
            movieRepository.save(movie);
            return;
        }

        double avgRating = reviews.stream()
                .mapToDouble(r -> r.getRating())
                .average()
                .orElse(0.0);

        // Round to 1 decimal place
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        Movie movie = findMovieById(movieId);
        movie.setRating(avgRating);
        movie.setTotalRatings(reviews.size());

        // Update trending score = (avg_rating * total_ratings) / 10
        double trendingScore = (avgRating * reviews.size()) / 10.0;
        movie.setTrendingScore(trendingScore);

        movieRepository.save(movie);
        log.debug("Rating updated for movie {}: {} ({} ratings)",
                movieId, avgRating, reviews.size());
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    public Movie findMovieById(String movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + movieId));
    }

    private List<Movie.CastMember> mapCastMembers(
            List<CreateMovieRequest.CastMemberRequest> castRequests) {
        if (castRequests == null) return new ArrayList<>();
        return castRequests.stream()
                .map(c -> Movie.CastMember.builder()
                        .name(c.getName())
                        .role(c.getRole())
                        .character(c.getCharacter())
                        .photoUrl(c.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Format duration from minutes to "Xh Ym" string.
     */
    private String formatDuration(Integer minutes) {
        if (minutes == null) return "";
        int hours = minutes / 60;
        int mins  = minutes % 60;
        if (hours == 0) return mins + "m";
        if (mins  == 0) return hours + "h";
        return hours + "h " + mins + "m";
    }

    public MovieResponse mapToMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .duration(movie.getDuration())
                .durationFormatted(formatDuration(movie.getDuration()))
                .languages(movie.getLanguages())
                .releaseDate(movie.getReleaseDate())
                .genres(movie.getGenres())
                .cast(movie.getCast() == null ? List.of() :
                        movie.getCast().stream()
                                .map(c -> MovieResponse.CastMemberDto.builder()
                                        .name(c.getName())
                                        .role(c.getRole())
                                        .character(c.getCharacter())
                                        .photoUrl(c.getPhotoUrl())
                                        .build())
                                .collect(Collectors.toList()))
                .director(movie.getDirector())
                .producer(movie.getProducer())
                .rating(movie.getRating())
                .totalRatings(movie.getTotalRatings())
                .posterUrl(movie.getPosterUrl())
                .bannerUrl(movie.getBannerUrl())
                .trailerUrl(movie.getTrailerUrl())
                .certificate(movie.getCertificate())
                .formats(movie.getFormats())
                .isActive(movie.getIsActive())
                .isUpcoming(movie.getIsUpcoming())
                .trendingScore(movie.getTrendingScore())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }

    public MovieSummaryResponse mapToSummaryResponse(Movie movie) {
        return MovieSummaryResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .duration(movie.getDuration())
                .durationFormatted(formatDuration(movie.getDuration()))
                .languages(movie.getLanguages())
                .releaseDate(movie.getReleaseDate())
                .genres(movie.getGenres())
                .director(movie.getDirector())
                .rating(movie.getRating())
                .totalRatings(movie.getTotalRatings())
                .posterUrl(movie.getPosterUrl())
                .certificate(movie.getCertificate())
                .formats(movie.getFormats())
                .isUpcoming(movie.getIsUpcoming())
                .build();
    }
}