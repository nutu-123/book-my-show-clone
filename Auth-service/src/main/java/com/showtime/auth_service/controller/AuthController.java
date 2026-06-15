package com.showtime.auth_service.controller;

import com.showtime.auth_service.dto.request.LoginRequest;
import com.showtime.auth_service.dto.request.RefreshTokenRequest;
import com.showtime.auth_service.dto.request.RegisterRequest;
import com.showtime.auth_service.dto.response.ApiResponse;
import com.showtime.auth_service.dto.response.AuthResponse;
import com.showtime.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller.
 *
 * Public endpoints:
 *  POST /api/auth/register   — create account
 *  POST /api/auth/login      — get tokens
 *  POST /api/auth/refresh    — refresh access token
 *
 * Secured endpoints:
 *  POST /api/auth/logout     — invalidate tokens
 *  GET  /api/auth/validate   — validate current token
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    // ─────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/auth/register
     * Register a new user account.
     *
     * Body: { name, email, password, phone }
     * Response: { accessToken, refreshToken, userId, roles, ... }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request for: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse,
                        "Account created successfully. Welcome to ShowTime!"));
    }

    // ─────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/auth/login
     * Authenticate and receive JWT tokens.
     *
     * Body: { email, password }
     * Response: { accessToken, refreshToken, userId, roles, ... }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(authResponse, "Login successful"));
    }

    // ─────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/auth/refresh
     * Get a new access token using the refresh token.
     *
     * Body: { refreshToken }
     * Response: { accessToken, refreshToken (new), ... }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.success(authResponse, "Token refreshed successfully"));
    }

    // ─────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/auth/logout
     * Invalidate all refresh tokens (logout from all devices).
     *
     * Header: Authorization: Bearer {accessToken}
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") String userId) {

        authService.logout(Long.parseLong(userId));
        return ResponseEntity.ok(
                ApiResponse.success(null, "Logged out successfully"));
    }

    // ─────────────────────────────────────────────────────────
    // VALIDATE (used by other services)
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/auth/validate
     * Validate the current JWT token.
     * Called by other microservices to verify tokens.
     *
     * Header: Authorization: Bearer {accessToken}
     * Response: { userId, email, roles }
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Object>> validateToken(
            @RequestHeader("X-User-Id")    String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") String roles) {

        var data = new java.util.HashMap<String, Object>();
        data.put("userId", userId);
        data.put("email",  email);
        data.put("roles",  roles);
        data.put("valid",  true);

        return ResponseEntity.ok(ApiResponse.success(data, "Token is valid"));
    }
}