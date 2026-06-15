package com.showtime.show_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full theatre detail response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TheatreResponse {

    private String       id;
    private String       name;
    private String       city;
    private String       state;
    private String       address;
    private String       pincode;
    private String       phone;
    private String       email;
    private List<String> amenities;
    private Integer      totalScreens;
    private Boolean      isActive;
    private Double       latitude;
    private Double       longitude;
    private String       googleMapsUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}