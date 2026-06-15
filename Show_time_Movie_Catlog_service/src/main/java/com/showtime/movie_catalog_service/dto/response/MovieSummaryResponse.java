package com.showtime.movie_catalog_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight movie response for listing pages.
 * Omits cast, description, and other heavy fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieSummaryResponse {

    private String       id;
    private String       title;
    private Integer      duration;
    private String       durationFormatted;
    private List<String> languages;
    private LocalDate    releaseDate;
    private List<String> genres;
    private String       director;
    private Double       rating;
    private Integer      totalRatings;
    private String       posterUrl;
    private String       certificate;
    private List<String> formats;
    private Boolean      isUpcoming;
}