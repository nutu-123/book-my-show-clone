package com.showtime.movie_catalog_service.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for POST /api/movies/{id}/reviews
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Minimum rating is 1.0")
    @DecimalMax(value = "5.0", message = "Maximum rating is 5.0")
    private Double rating;

    @Size(max = 100, message = "Review title max 100 characters")
    private String title;

    @NotBlank(message = "Review comment is required")
    @Size(min = 10, max = 1000, message = "Comment must be 10–1000 characters")
    private String comment;
}