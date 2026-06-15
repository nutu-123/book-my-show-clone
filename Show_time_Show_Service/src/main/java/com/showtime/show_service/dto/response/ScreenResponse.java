package com.showtime.show_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.showtime.show_service.document.Screen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Screen detail response — includes full seat layout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenResponse {

    private String              id;
    private String              theatreId;
    private String              screenName;
    private Integer             screenNumber;
    private Integer             totalSeats;
    private Screen.SeatLayout   seatLayout;
    private List<String>        supportedFormats;
    private Boolean             isActive;
    private LocalDateTime       createdAt;
}