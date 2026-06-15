package com.showtime.movie_catalog_service.controller;

import com.showtime.movie_catalog_service.constants.AppConstants;
import com.showtime.movie_catalog_service.dto.request.CreateReviewRequest;
import com.showtime.movie_catalog_service.dto.response.ApiResponse;
import com.showtime.movie_catalog_service.dto.response.ReviewResponse;
import com.showtime.movie_catalog_service.service.ReviewService;
import jakarta.validation.Valid;
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

/**
 * Review REST controller.
 *
 * GET    /api/movies/{id}/reviews          — list reviews (public)
 * GET    /api/movies/{id}/reviews/my       — my review (JWT)
 * POST   /api/movies/{id}/reviews          — add review (JWT)
 * PUT    /api/movies/{id}/reviews/{rid}    — update review (JWT)
 * DELETE /api/movies/{id}/reviews/{rid}    — delete review (JWT/Admin)
 */
@RestController
@RequestMapping("/api/movies/{movieId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponse> reviews =
                reviewService.getMovieReviews(movieId, page, size);

        ApiResponse<Page<ReviewResponse>> response =
                ApiResponse.<Page<ReviewResponse>>builder()
                        .success(true)
                        .message("Reviews fetched")
                        .data(reviews)
                        .page(reviews.getNumber())
                        .size(reviews.getSize())
                        .totalElements(reviews.getTotalElements())
                        .totalPages(reviews.getTotalPages())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(
            @PathVariable String movieId,
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId) {

        ReviewResponse review =
                reviewService.getUserReviewForMovie(movieId, userId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @PathVariable String movieId,
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader(AppConstants.HEADER_USER_ID)    String userId,
            @RequestHeader(AppConstants.HEADER_USER_EMAIL) String userEmail,
            @RequestHeader(value = "X-User-Name",
                           defaultValue = "User") String userName) {

        ReviewResponse review = reviewService.createReview(
                movieId, request, userId, userName, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Review added successfully"));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable String movieId,
            @PathVariable String reviewId,
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId) {

        ReviewResponse updated =
                reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Review updated successfully"));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable String movieId,
            @PathVariable String reviewId,
            @RequestHeader(AppConstants.HEADER_USER_ID)    String userId,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        boolean isAdmin = roles.contains(AppConstants.ROLE_ADMIN);
        reviewService.deleteReview(reviewId, userId, isAdmin);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Review deleted successfully"));
    }
}