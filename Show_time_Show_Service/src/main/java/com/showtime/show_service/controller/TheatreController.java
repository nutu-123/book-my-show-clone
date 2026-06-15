package com.showtime.show_service.controller;

import com.showtime.show_service.constants.AppConstants;
import com.showtime.show_service.dto.request.CreateTheatreRequest;
import com.showtime.show_service.dto.request.UpdateTheatreRequest;
import com.showtime.show_service.dto.response.ApiResponse;
import com.showtime.show_service.dto.response.TheatreResponse;
import com.showtime.show_service.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Theatre REST controller.
 *
 * Public:
 *   GET /api/theatres                   — list all
 *   GET /api/theatres/{id}              — detail
 *   GET /api/theatres/city/{city}       — by city
 *   GET /api/theatres/cities            — all cities
 *
 * Admin:
 *   POST   /api/theatres                — create
 *   PUT    /api/theatres/{id}           — update
 *   DELETE /api/theatres/{id}           — soft delete
 */
@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    @Autowired
    private TheatreService theatreService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TheatreResponse>>> getAllTheatres(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TheatreResponse> theatres =
                theatreService.getAllTheatres(page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<TheatreResponse>>builder()
                        .success(true).message("Theatres fetched")
                        .data(theatres)
                        .page(theatres.getNumber())
                        .size(theatres.getSize())
                        .totalElements(theatres.getTotalElements())
                        .totalPages(theatres.getTotalPages())
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TheatreResponse>> getById(
            @PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success(theatreService.getTheatreById(id)));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<TheatreResponse>>> getByCity(
            @PathVariable String city) {
        List<TheatreResponse> theatres =
                theatreService.getAllTheatresInCity(city);
        return ResponseEntity.ok(
                ApiResponse.success(theatres, "Theatres in " + city));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getAllCities() {
        List<String> cities = theatreService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities, "Cities fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TheatreResponse>> createTheatre(
            @Valid @RequestBody CreateTheatreRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        TheatreResponse created = theatreService.createTheatre(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created,
                        "Theatre created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TheatreResponse>> updateTheatre(
            @PathVariable String id,
            @RequestBody UpdateTheatreRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        TheatreResponse updated = theatreService.updateTheatre(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Theatre updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTheatre(
            @PathVariable String id,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        theatreService.deleteTheatre(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Theatre deleted successfully"));
    }

    private void validateAdmin(String roles) {
        if (!roles.contains(AppConstants.ROLE_ADMIN)) {
            throw new org.springframework.security.access
                    .AccessDeniedException("Admin role required.");
        }
    }
}