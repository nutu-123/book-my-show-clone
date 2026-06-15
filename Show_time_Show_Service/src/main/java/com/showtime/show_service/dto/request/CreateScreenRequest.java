package com.showtime.show_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for POST /api/theatres/{id}/screens (Admin)
 *
 * Example payload:
 * {
 *   "screenName": "Screen 1",
 *   "screenNumber": 1,
 *   "supportedFormats": ["2D", "3D"],
 *   "seatLayout": {
 *     "rows": 10,
 *     "columns": 12,
 *     "aisleAfterColumns": [4, 8],
 *     "seatCategories": [
 *       {
 *         "type": "STANDARD",
 *         "rows": ["A","B","C","D","E"],
 *         "seatsPerRow": 12,
 *         "price": 180.00,
 *         "colorCode": "#4CAF50"
 *       },
 *       {
 *         "type": "PREMIUM",
 *         "rows": ["F","G","H"],
 *         "seatsPerRow": 12,
 *         "price": 250.00,
 *         "colorCode": "#2196F3"
 *       },
 *       {
 *         "type": "RECLINER",
 *         "rows": ["I","J"],
 *         "seatsPerRow": 12,
 *         "price": 400.00,
 *         "colorCode": "#9C27B0"
 *       }
 *     ]
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScreenRequest {

    @NotBlank(message = "Screen name is required")
    private String screenName;

    @NotNull(message = "Screen number is required")
    @Min(value = 1, message = "Screen number must be at least 1")
    private Integer screenNumber;

    @NotEmpty(message = "At least one format is required")
    private List<String> supportedFormats;

    @Valid
    @NotNull(message = "Seat layout is required")
    private SeatLayoutRequest seatLayout;

    // ── Embedded seat layout ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatLayoutRequest {

        @NotNull
        @Min(value = 1, message = "Rows must be at least 1")
        private Integer rows;

        @NotNull
        @Min(value = 1, message = "Columns must be at least 1")
        private Integer columns;

        @Builder.Default
        private List<Integer> aisleAfterColumns = new ArrayList<>();

        @Valid
        @NotEmpty(message = "At least one seat category is required")
        private List<SeatCategoryRequest> seatCategories;

        // Lombok @Builder.Default doesn't work on inner static classes
        // without @Builder on outer. Using field initializer instead.
        {
            aisleAfterColumns = new ArrayList<>();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatCategoryRequest {

        @NotBlank(message = "Seat type is required (STANDARD/PREMIUM/RECLINER)")
        private String type;

        @NotEmpty(message = "Row labels are required")
        private List<String> rows;

        @NotNull(message = "Seats per row is required")
        @Min(value = 1)
        private Integer seatsPerRow;

        @NotNull(message = "Price is required")
        private Double price;

        private String colorCode;
    }
}