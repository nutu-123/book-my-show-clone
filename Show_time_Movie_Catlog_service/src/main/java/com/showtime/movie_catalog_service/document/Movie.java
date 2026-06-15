package com.showtime.movie_catalog_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Movie MongoDB document.
 * Collection: "movies"
 *
 * Indexes:
 *  - Text index on title + description (for search)
 *  - Index on genres (for filtering)
 *  - Index on releaseDate (for sorting/trending)
 *  - Index on isActive (for filtering active movies)
 */
@Document(collection = "movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    private String id;

    @TextIndexed(weight = 3)           // Higher weight = more relevant in text search
    @Field("title")
    private String title;

    @TextIndexed(weight = 1)
    @Field("description")
    private String description;

    @Field("duration")
    private Integer duration;          // Duration in minutes

    @Field("language")
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @Indexed
    @Field("release_date")
    private LocalDate releaseDate;

    @Indexed
    @Field("genres")
    @Builder.Default
    private List<String> genres = new ArrayList<>();

    @Field("cast")
    @Builder.Default
    private List<CastMember> cast = new ArrayList<>();

    @Field("director")
    private String director;

    @Field("producer")
    private String producer;

    @Field("rating")
    @Builder.Default
    private Double rating = 0.0;       // Average rating (1–5)

    @Field("total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Field("poster_url")
    private String posterUrl;

    @Field("banner_url")
    private String bannerUrl;

    @Field("trailer_url")
    private String trailerUrl;

    @Field("certificate")
    private String certificate;        // U, UA, A, S

    @Field("formats")
    @Builder.Default
    private List<String> formats = new ArrayList<>();  // 2D, 3D, IMAX

    @Indexed
    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Field("is_upcoming")
    @Builder.Default
    private Boolean isUpcoming = false;

    @Field("trending_score")
    @Builder.Default
    private Double trendingScore = 0.0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // ── Embedded document for cast members ──
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CastMember {
        private String name;
        private String role;       // e.g., "Lead Actor", "Supporting"
        private String character;  // character name in the movie
        private String photoUrl;
    }
}