package com.showtime.show_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for PUT /api/theatres/{id} (Admin)
 * All fields optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTheatreRequest {

    private String       name;
    private String       city;
    private String       state;
    private String       address;
    private String       pincode;
    private String       phone;
    private String       email;
    private List<String> amenities;
    private Double       latitude;
    private Double       longitude;
    private String       googleMapsUrl;
    private Boolean      isActive;
}