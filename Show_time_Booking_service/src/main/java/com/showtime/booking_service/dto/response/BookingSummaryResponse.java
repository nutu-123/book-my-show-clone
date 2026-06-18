package com.showtime.booking_service.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight booking summary for listing pages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingSummaryResponse {

    private Long         id;
    private String       bookingReference;
    private String       movieTitle;
    private String       theatreName;
    private String       screenName;
    private String       showDate;
    private String       showTime;
    private String       language;
    private String       format;
    private List<String> seatNumbers;
    private Integer      totalSeats;
    private BigDecimal   grandTotal;
    private String       status;
    private LocalDateTime bookingTime;
    private LocalDateTime confirmedTime;
}