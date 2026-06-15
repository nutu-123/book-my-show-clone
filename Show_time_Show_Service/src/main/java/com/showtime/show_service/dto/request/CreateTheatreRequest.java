package com.showtime.show_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for POST /api/theatres (Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTheatreRequest {

    @NotBlank(message = "Theatre name is required")
    @Size(min = 2, max = 150, message = "Name must be 2–150 characters")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Address is required")
    private String address;

    private String pincode;
    private String phone;
    private String email;

    private List<String> amenities = new ArrayList<>();

    private Double latitude;
    private Double longitude;
    private String googleMapsUrl;
}