package com.showtime.show_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen MongoDB document.
 * Collection: "screens"
 *
 * Represents a single screen inside a theatre.
 * Contains the full seat layout configuration.
 *
 * Seat Layout Design:
 * Rows are labeled A, B, C... Z, AA, AB...
 * Seats per row are numbered 1, 2, 3...
 * Each row group has a seat type (STANDARD/PREMIUM/RECLINER)
 * and a price.
 *
 * Example for a 100-seat screen:
 *   Rows A-E (50 seats): STANDARD   @ ₹180
 *   Rows F-H (30 seats): PREMIUM    @ ₹250
 *   Rows I-J (20 seats): RECLINER   @ ₹400
 */
@Document(collection = "screens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screen {

    @Id
    private String id;

    @Indexed
    @Field("theatre_id")
    private String theatreId;

    @Field("screen_name")
    private String screenName;         // e.g., "Screen 1", "Audi 1"

    @Field("screen_number")
    private Integer screenNumber;

    @Field("total_seats")
    private Integer totalSeats;

    @Field("seat_layout")
    private SeatLayout seatLayout;

    @Field("supported_formats")
    @Builder.Default
    private List<String> supportedFormats = new ArrayList<>();
    // e.g., ["2D", "3D", "IMAX"]

    @Indexed
    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // ── Embedded: SeatLayout ──
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatLayout {

        @Field("rows")
        private Integer rows;

        @Field("columns")
        private Integer columns;

        @Field("seat_categories")
        @Builder.Default
        private List<SeatCategory> seatCategories = new ArrayList<>();

        @Field("aisle_after_columns")
        @Builder.Default
        private List<Integer> aisleAfterColumns = new ArrayList<>();
        // Column numbers after which there's an aisle
        // e.g., [5, 10] means aisle after column 5 and 10
    }

    // ── Embedded: SeatCategory (group of rows with same type/price) ──
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatCategory {

        @Field("type")
        private String type;           // STANDARD, PREMIUM, RECLINER

        @Field("rows")
        private List<String> rows;     // e.g., ["A","B","C","D","E"]

        @Field("seats_per_row")
        private Integer seatsPerRow;

        @Field("price")
        private Double price;          // Base price in INR

        @Field("color_code")
        private String colorCode;      // UI color: "#4CAF50", "#2196F3", "#9C27B0"
    }
}