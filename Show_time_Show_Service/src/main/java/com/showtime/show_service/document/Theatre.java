package com.showtime.show_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Theatre MongoDB document.
 * Collection: "theatres"
 *
 * Represents a physical cinema theatre with
 * multiple screens.
 */
@Document(collection = "theatres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Theatre {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Indexed
    @Field("city")
    private String city;

    @Field("state")
    private String state;

    @Field("address")
    private String address;

    @Field("pincode")
    private String pincode;

    @Field("phone")
    private String phone;

    @Field("email")
    private String email;

    @Field("amenities")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();
    // e.g., ["Parking", "Food Court", "Wheelchair Access", "3D"]

    @Field("total_screens")
    @Builder.Default
    private Integer totalScreens = 0;

    @Indexed
    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Field("latitude")
    private Double latitude;

    @Field("longitude")
    private Double longitude;

    @Field("google_maps_url")
    private String googleMapsUrl;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}