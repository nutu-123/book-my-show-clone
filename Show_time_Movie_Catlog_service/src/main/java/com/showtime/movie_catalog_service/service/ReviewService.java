package com.showtime.movie_catalog_service.service;


import com.showtime.movie_catalog_service.document.Review;
import com.showtime.movie_catalog_service.dto.request.CreateReviewRequest;
import com.showtime.movie_catalog_service.dto.response.ReviewResponse;
import com.showtime.movie_catalog_service.exception.ResourceNotFoundException;
import com.showtime.movie_catalog_service.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service for movie reviews.
 *
 * Business rules:
 *  - One review per user per movie
 *  - Rating triggers recalculation of movie average
 *  - Users can update or delete their own review
 */
@Service
public class ReviewService {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewService.class);

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private MovieService     movieService;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    /**
     * Add a review for a movie.
     * One review per user per movie — throws if already reviewed.
     */
    public ReviewResponse createReview(String movieId,
                                        CreateReviewRequest request,
                                        String userId,
                                        String userName,
                                        String userEmail) {
        log.info("Creating review for movie {} by user {}", movieId, userId);

        // Verify movie exists
        movieService.findMovieById(movieId);

        // Check duplicate
        if (reviewRepository.existsByMovieIdAndUserId(movieId, userId)) {
            throw new IllegalStateException(
                    "You have already reviewed this movie. " +
                    "Please update your existing review.");
        }

        Review review = Review.builder()
                .movieId(movieId)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerified(false)
                .likes(0)
                .build();

        Review saved = reviewRepository.save(review);

        // Recalculate movie average rating
        movieService.recalculateRating(movieId);

        log.info("Review created: {} for movie {}", saved.getId(), movieId);
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    /**
     * Get all reviews for a movie (paginated).
     */
    public Page<ReviewResponse> getMovieReviews(
            String movieId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByMovieId(movieId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get the logged-in user's review for a movie.
     */
    public ReviewResponse getUserReviewForMovie(
            String movieId, String userId) {

        Review review = reviewRepository
                .findByMovieIdAndUserId(movieId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You haven't reviewed this movie yet."));
        return mapToResponse(review);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────

    /**
     * Update a review — only the author can update.
     */
    public ReviewResponse updateReview(String reviewId,
                                        CreateReviewRequest request,
                                        String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new SecurityException(
                    "You are not authorized to update this review.");
        }

        review.setRating(request.getRating());
        if (request.getTitle()   != null) review.setTitle(request.getTitle());
        if (request.getComment() != null) review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);

        // Recalculate movie rating
        movieService.recalculateRating(review.getMovieId());

        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────

    /**
     * Delete a review — only the author or admin can delete.
     */
    public void deleteReview(String reviewId, String userId,
                              boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found: " + reviewId));

        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new SecurityException(
                    "You are not authorized to delete this review.");
        }

        String movieId = review.getMovieId();
        reviewRepository.delete(review);

        // Recalculate movie rating
        movieService.recalculateRating(movieId);

        log.info("Review deleted: {}", reviewId);
    }

    // ─────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .movieId(review.getMovieId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerified(review.getIsVerified())
                .likes(review.getLikes())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}