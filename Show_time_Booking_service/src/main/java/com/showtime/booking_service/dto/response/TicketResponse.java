package com.showtime.booking_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Individual ticket response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketResponse {

    private Long       id;
    private String     ticketNumber;
    private String     seatNumber;
    private String     seatType;
    private String     rowLabel;
    private Integer    columnNumber;
    private BigDecimal price;
    private String     status;
}