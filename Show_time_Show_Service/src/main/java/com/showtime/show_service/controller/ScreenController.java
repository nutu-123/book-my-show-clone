package com.showtime.show_service.controller;


import com.showtime.show_service.constants.AppConstants;
import com.showtime.show_service.dto.request.CreateScreenRequest;
import com.showtime.show_service.dto.response.ApiResponse;
import com.showtime.show_service.dto.response.ScreenResponse;
import com.showtime.show_service.service.ScreenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Screen REST controller.
 *
 * Public:
 *   GET /api/theatres/{theatreId}/screens      — list screens
 *   GET /api/theatres/{theatreId}/screens/{id} — screen detail
 *
 * Admin:
 *   POST   /api/theatres/{theatreId}/screens      — create
 *   DELETE /api/theatres/{theatreId}/screens/{id} — delete
 */
@RestController
@RequestMapping("/api/theatres/{theatreId}/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScreenResponse>>> getScreens(
            @PathVariable String theatreId) {

        List<ScreenResponse> screens =
                screenService.getScreensByTheatre(theatreId);
        return ResponseEntity.ok(
                ApiResponse.success(screens, "Screens fetched"));
    }

    @GetMapping("/{screenId}")
    public ResponseEntity<ApiResponse<ScreenResponse>> getScreen(
            @PathVariable String theatreId,
            @PathVariable String screenId) {

        ScreenResponse screen = screenService.getScreenById(screenId);
        return ResponseEntity.ok(ApiResponse.success(screen));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScreenResponse>> createScreen(
            @PathVariable String theatreId,
            @Valid @RequestBody CreateScreenRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        ScreenResponse created = screenService.createScreen(theatreId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created,
                        "Screen created successfully"));
    }

    @DeleteMapping("/{screenId}")
    public ResponseEntity<ApiResponse<Void>> deleteScreen(
            @PathVariable String theatreId,
            @PathVariable String screenId,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        screenService.deleteScreen(screenId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Screen deleted successfully"));
    }

    private void validateAdmin(String roles) {
        if (!roles.contains(AppConstants.ROLE_ADMIN)) {
            throw new org.springframework.security.access
                    .AccessDeniedException("Admin role required.");
        }
    }
}