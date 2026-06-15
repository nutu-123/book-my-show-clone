package com.showtime.show_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.showtime.show_service.document.Show;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Full show detail response — includes seat layout and prices.
 * Used for the seat selection page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShowResponse {

    private String                   id;
    private String                   movieId;
    private String                   movieTitle;
    private String                   moviePosterUrl;
    private String                   theatreId;
    private String                   theatreName;
    private String                   screenId;
    private String                   screenName;
    private String                   city;
    private LocalDate                showDate;
    private LocalTime                startTime;
    private LocalTime                endTime;
    private String                   language;
    private String                   format;
    private Integer                  totalSeats;
    private Integer                  availableSeats;
    private List<String>             bookedSeats;
    private List<Show.SeatPriceInfo> seatPrices;
    private String                   status;
    private Boolean                  isActive;

    // Full screen seat layout — needed for seat selection UI
    private ScreenResponse           screen;

    private LocalDateTime            createdAt;
}