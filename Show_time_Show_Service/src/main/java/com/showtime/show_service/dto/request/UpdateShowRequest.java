package com.showtime.show_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for PUT /api/shows/{id} (Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShowRequest {

    private LocalDate showDate;
    private LocalTime startTime;
    private String    language;
    private String    format;
    private String    status;     // ACTIVE, CANCELLED
    private Boolean   isActive;
}