package com.showtime.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user profile endpoints.
 * Does NOT include password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long            id;
    private String          name;
    private String          email;
    private String          phone;
    private Boolean         enabled;
    private List<String>    roles;
    private LocalDateTime   createdAt;
    private LocalDateTime   updatedAt;
}