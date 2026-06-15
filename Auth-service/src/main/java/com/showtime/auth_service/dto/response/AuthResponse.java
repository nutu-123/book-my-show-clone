package com.showtime.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for login and refresh token endpoints.
 * Contains both access token and refresh token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String       accessToken;
    private String       refreshToken;
    private String       tokenType;
    private long         expiresIn;      // seconds
    private Long         userId;
    private String       email;
    private String       name;
    private List<String> roles;
}