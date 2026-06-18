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
 * Full booking detail response.
 * Includes all tickets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private Long              id;
    private String            bookingReference;
    private Long              userId;
    private String            userEmail;
    private String            showId;
    private String            movieId;
    private String            movieTitle;
    private String            theatreId;
    private String            theatreName;
    private String            screenId;
    private String            screenName;
    private String            showDate;
    private String            showTime;
    private String            language;
    private String            format;
    private BigDecimal        totalAmount;
    private BigDecimal        convenienceFee;
    private BigDecimal        grandTotal;
    private Integer           totalSeats;
    private String            status;
    private Long              paymentId;
    private List<TicketResponse> tickets;
    private List<String>      seatNumbers;
    private LocalDateTime     bookingTime;
    private LocalDateTime     expiryTime;
    private LocalDateTime     confirmedTime;
    private LocalDateTime     cancelledTime;
    private String            cancellationReason;
}