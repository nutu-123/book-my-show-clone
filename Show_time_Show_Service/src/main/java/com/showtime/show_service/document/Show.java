package com.showtime.show_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Show MongoDB document.
 * Collection: "shows"
 *
 * Represents a specific screening of a movie
 * at a specific screen on a specific date/time.
 *
 * Compound index on (screenId + showDate + startTime)
 * prevents double-booking of a screen.
 */
@Document(collection = "shows")
@CompoundIndexes({
    @CompoundIndex(
        name   = "screen_date_time_idx",
        def    = "{'screen_id':1, 'show_date':1, 'start_time':1}",
        unique = true
    ),
    @CompoundIndex(
        name = "movie_date_city_idx",
        def  = "{'movie_id':1, 'show_date':1, 'city':1}"
    )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    @Id
    private String id;

    @Indexed
    @Field("movie_id")
    private String movieId;

    @Field("movie_title")
    private String movieTitle;          // Denormalized for query performance

    @Field("movie_poster_url")
    private String moviePosterUrl;      // Denormalized

    @Indexed
    @Field("theatre_id")
    private String theatreId;

    @Field("theatre_name")
    private String theatreName;         // Denormalized

    @Indexed
    @Field("screen_id")
    private String screenId;

    @Field("screen_name")
    private String screenName;          // Denormalized

    @Indexed
    @Field("city")
    private String city;                // Denormalized from Theatre

    @Indexed
    @Field("show_date")
    private LocalDate showDate;

    @Field("start_time")
    private LocalTime startTime;

    @Field("end_time")
    private LocalTime endTime;

    @Field("language")
    private String language;

    @Field("format")
    private String format;              // 2D, 3D, IMAX, 4DX

    @Field("total_seats")
    private Integer totalSeats;

    @Field("available_seats")
    private Integer availableSeats;

    @Field("booked_seats")
    @Builder.Default
    private List<String> bookedSeats = new ArrayList<>();
    // Seat numbers that are CONFIRMED booked: ["A1","A2","B3"]

    @Field("seat_prices")
    @Builder.Default
    private List<SeatPriceInfo> seatPrices = new ArrayList<>();
    // Copied from screen at time of show creation

    @Indexed
    @Field("status")
    @Builder.Default
    private String status = "ACTIVE";   // ACTIVE, CANCELLED, HOUSEFULL, COMPLETED

    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // ── Embedded: seat price info (snapshot from Screen) ──
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatPriceInfo {
        private String seatType;    // STANDARD, PREMIUM, RECLINER
        private Double price;
        private String colorCode;
    }

    /**
     * Helper — check if a seat is already booked.
     */
    public boolean isSeatBooked(String seatNumber) {
        return bookedSeats != null && bookedSeats.contains(seatNumber);
    }

    /**
     * Helper — check if show is housefull.
     */
    public boolean isHousefull() {
        return availableSeats != null && availableSeats <= 0;
    }
}