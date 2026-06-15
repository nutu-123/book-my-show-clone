package com.showtime.movie_catalog_service.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private String        id;
    private String        movieId;
    private String        userId;
    private String        userName;
    private Double        rating;
    private String        title;
    private String        comment;
    private Boolean       isVerified;
    private Integer       likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}