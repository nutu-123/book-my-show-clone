package com.showtime.movie_catalog_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full movie detail response — returned for single movie endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieResponse {

    private String              id;
    private String              title;
    private String              description;
    private Integer             duration;          // minutes
    private String              durationFormatted; // "2h 30m"
    private List<String>        languages;
    private LocalDate           releaseDate;
    private List<String>        genres;
    private List<CastMemberDto> cast;
    private String              director;
    private String              producer;
    private Double              rating;
    private Integer             totalRatings;
    private String              posterUrl;
    private String              bannerUrl;
    private String              trailerUrl;
    private String              certificate;
    private List<String>        formats;
    private Boolean             isActive;
    private Boolean             isUpcoming;
    private Double              trendingScore;
    private LocalDateTime       createdAt;
    private LocalDateTime       updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CastMemberDto {
        private String name;
        private String role;
        private String character;
        private String photoUrl;
    }
}