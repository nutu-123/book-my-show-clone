package com.showtime.show_service.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for POST /api/shows (Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowRequest {

    @NotBlank(message = "Movie ID is required")
    private String movieId;

    @NotBlank(message = "Theatre ID is required")
    private String theatreId;

    @NotBlank(message = "Screen ID is required")
    private String screenId;

    @NotNull(message = "Show date is required")
    @FutureOrPresent(message = "Show date must be today or in the future")
    private LocalDate showDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Format is required (2D/3D/IMAX)")
    private String format;

    // Movie title and poster are fetched from movie-catalog-service
    // These are optional overrides
    private String movieTitle;
    private String moviePosterUrl;
}