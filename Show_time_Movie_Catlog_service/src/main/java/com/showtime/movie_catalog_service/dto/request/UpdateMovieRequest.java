package com.showtime.movie_catalog_service.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for PUT /api/movies/{id} (Admin)
 * All fields optional — only non-null fields are updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieRequest {

    private String       title;
    private String       description;
    private Integer      duration;
    private List<String> languages;
    private LocalDate    releaseDate;
    private List<String> genres;
    private List<CreateMovieRequest.CastMemberRequest> cast;
    private String       director;
    private String       producer;
    private String       posterUrl;
    private String       bannerUrl;
    private String       trailerUrl;
    private String       certificate;
    private List<String> formats;
    private Boolean      isActive;
    private Boolean      isUpcoming;
}