package com.showtime.auth_service.controller;


import com.showtime.auth_service.dto.response.ApiResponse;
import com.showtime.auth_service.dto.response.UserResponse;
import com.showtime.auth_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User profile controller.
 *
 * GET  /api/auth/me          — get own profile (JWT required)
 * PUT  /api/auth/me          — update own profile (JWT required)
 * GET  /api/users/admin/all  — list all users (ADMIN only)
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * GET /api/auth/me
     * Get the logged-in user's profile.
     * userId comes from X-User-Id header set by API Gateway.
     */
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {

        UserResponse user = userService.getUserById(Long.parseLong(userId));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * PUT /api/auth/me
     * Update name and/or phone of logged-in user.
     */
    @PutMapping("/auth/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone) {

        UserResponse updated = userService.updateUser(
                Long.parseLong(userId), name, phone);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Profile updated successfully"));
    }

    /**
     * GET /api/users/admin/all
     * List all users — ADMIN only.
     */
    @GetMapping("/users/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}