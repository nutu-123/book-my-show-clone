package com.showtime.movie_catalog_service.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for POST /api/movies (Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be 1–200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be 10–2000 characters")
    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotEmpty(message = "At least one language is required")
    private List<String> languages;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotEmpty(message = "At least one genre is required")
    private List<String> genres;

    private List<CastMemberRequest> cast = new ArrayList<>();

    @NotBlank(message = "Director is required")
    private String director;

    private String producer;

    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;

    private String certificate;   // U, UA, A, S

    private List<String> formats = new ArrayList<>();

    private Boolean isUpcoming = false;

    // ── Embedded cast member ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CastMemberRequest {

        @NotBlank(message = "Cast member name is required")
        private String name;
        private String role;
        private String character;
        private String photoUrl;
    }
}