package com.showtime.show_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.showtime.show_service.document.Show;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Lightweight show response for theatre selection page.
 * Used when listing shows for a movie+city+date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShowSummaryResponse {

    private String                   id;
    private String                   movieId;
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
    private String                   status;
    private List<Show.SeatPriceInfo> seatPrices;

    // Computed
    private Boolean                  isAvailable;   // availableSeats > 0
    private String                   startTimeFormatted;  // "10:30 AM"
}