package com.showtime.movie_catalog_service.controller;

import com.showtime.movie_catalog_service.constants.AppConstants;
import com.showtime.movie_catalog_service.dto.request.CreateMovieRequest;
import com.showtime.movie_catalog_service.dto.request.UpdateMovieRequest;
import com.showtime.movie_catalog_service.dto.response.ApiResponse;
import com.showtime.movie_catalog_service.dto.response.MovieResponse;
import com.showtime.movie_catalog_service.dto.response.MovieSummaryResponse;
import com.showtime.movie_catalog_service.service.MovieService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Movie catalog REST controller.
 *
 * Public endpoints (no JWT):
 *   GET  /api/movies                    — list all
 *   GET  /api/movies/{id}               — movie detail
 *   GET  /api/movies/trending           — trending
 *   GET  /api/movies/upcoming           — upcoming
 *   GET  /api/movies/top-rated          — top rated
 *   GET  /api/movies/search             — search
 *   GET  /api/movies/filter             — filter
 *   GET  /api/movies/genres             — all genres
 *   GET  /api/movies/languages          — all languages
 *
 * Admin endpoints (ROLE_ADMIN required):
 *   POST   /api/movies                  — create
 *   PUT    /api/movies/{id}             — update
 *   DELETE /api/movies/{id}             — soft delete
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private static final Logger log =
            LoggerFactory.getLogger(MovieController.class);

    @Autowired
    private MovieService movieService;

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MovieSummaryResponse>>> getAllMovies(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieSummaryResponse> movies =
                movieService.getAllMovies(page, size);

        ApiResponse<Page<MovieSummaryResponse>> response =
                ApiResponse.<Page<MovieSummaryResponse>>builder()
                        .success(true)
                        .message("Movies fetched successfully")
                        .data(movies)
                        .page(movies.getNumber())
                        .size(movies.getSize())
                        .totalElements(movies.getTotalElements())
                        .totalPages(movies.getTotalPages())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(
            @PathVariable String id) {

        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(
                ApiResponse.success(movie, "Movie fetched successfully"));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<MovieSummaryResponse>>> getTrending() {
        List<MovieSummaryResponse> trending = movieService.getTrendingMovies();
        return ResponseEntity.ok(
                ApiResponse.success(trending, "Trending movies fetched"));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<Page<MovieSummaryResponse>>> getUpcoming(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieSummaryResponse> upcoming =
                movieService.getUpcomingMovies(page, size);
        return ResponseEntity.ok(
                ApiResponse.success(upcoming, "Upcoming movies fetched"));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<Page<MovieSummaryResponse>>> getTopRated(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieSummaryResponse> topRated =
                movieService.getTopRatedMovies(page, size);
        return ResponseEntity.ok(
                ApiResponse.success(topRated, "Top rated movies fetched"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MovieSummaryResponse>>> searchMovies(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieSummaryResponse> results =
                movieService.searchMovies(q, page, size);

        ApiResponse<Page<MovieSummaryResponse>> response =
                ApiResponse.<Page<MovieSummaryResponse>>builder()
                        .success(true)
                        .message("Search results for: " + q)
                        .data(results)
                        .page(results.getNumber())
                        .size(results.getSize())
                        .totalElements(results.getTotalElements())
                        .totalPages(results.getTotalPages())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<MovieSummaryResponse>>> filterMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String certificate,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieSummaryResponse> results =
                movieService.filterMovies(
                        genre, language, format, certificate, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(results, "Filtered movies fetched"));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getAllGenres() {
        List<String> genres = movieService.getAllGenres();
        return ResponseEntity.ok(
                ApiResponse.success(genres, "Genres fetched successfully"));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<String>>> getAllLanguages() {
        List<String> languages = movieService.getAllLanguages();
        return ResponseEntity.ok(
                ApiResponse.success(languages, "Languages fetched successfully"));
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(
            @Valid @RequestBody CreateMovieRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdminRole(roles);
        MovieResponse created = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Movie created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody UpdateMovieRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdminRole(roles);
        MovieResponse updated = movieService.updateMovie(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Movie updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(
            @PathVariable String id,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdminRole(roles);
        movieService.deleteMovie(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Movie deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────

    private void validateAdminRole(String roles) {
        if (!roles.contains(AppConstants.ROLE_ADMIN)) {
            throw new org.springframework.security.access
                    .AccessDeniedException(
                    "Access denied. Admin role required.");
        }
    }
}