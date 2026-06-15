package com.showtime.movie_catalog_service.document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Review MongoDB document.
 * Collection: "reviews"
 *
 * Compound index on (movieId + userId) ensures
 * one review per user per movie.
 */
@Document(collection = "reviews")
@CompoundIndex(
    name    = "movie_user_idx",
    def     = "{'movie_id': 1, 'user_id': 1}",
    unique  = true
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private String id;

    @Indexed
    @Field("movie_id")
    private String movieId;

    @Field("user_id")
    private String userId;

    @Field("user_name")
    private String userName;

    @Field("user_email")
    private String userEmail;

    @Field("rating")
    private Double rating;       // 1.0 to 5.0

    @Field("title")
    private String title;        // Short review title

    @Field("comment")
    private String comment;      // Full review text

    @Field("is_verified")
    @Builder.Default
    private Boolean isVerified = false;   // True if user has booked this movie

    @Field("likes")
    @Builder.Default
    private Integer likes = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}