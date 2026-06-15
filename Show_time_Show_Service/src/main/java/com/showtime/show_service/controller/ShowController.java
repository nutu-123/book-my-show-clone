package com.showtime.show_service.controller;

import com.showtime.show_service.constants.AppConstants;
import com.showtime.show_service.dto.request.CreateShowRequest;
import com.showtime.show_service.dto.request.UpdateShowRequest;
import com.showtime.show_service.dto.response.ApiResponse;
import com.showtime.show_service.dto.response.ShowResponse;
import com.showtime.show_service.dto.response.ShowSummaryResponse;
import com.showtime.show_service.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

/**
 * Show REST controller.
 *
 * Public:
 *   GET /api/shows?movieId=&city=&date=     — shows for booking
 *   GET /api/shows/{id}                     — show details + seat layout
 *   GET /api/shows/theatre/{id}?date=       — shows at a theatre
 *
 * Admin:
 *   POST   /api/shows           — create show
 *   PUT    /api/shows/{id}      — update show
 *   DELETE /api/shows/{id}      — cancel show
 *   GET    /api/shows/admin/all — all shows paginated
 *
 * Internal (called by Booking Service):
 *   PUT /api/shows/{id}/seats   — update seat availability
 */
@RestController
@RequestMapping("/api/shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    // ─────────────────────────────────────────────────────────
    // PUBLIC
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/shows?movieId={id}&city={city}&date={date}
     * Primary query for Theatre Selection page.
     * Returns all shows for a movie in a city on a given date.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowSummaryResponse>>> getShows(
            @RequestParam String movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                          LocalDate date) {

        List<ShowSummaryResponse> shows =
                showService.getShowsByMovieCityDate(movieId, city, date);

        return ResponseEntity.ok(
                ApiResponse.<List<ShowSummaryResponse>>builder()
                        .success(true)
                        .message(shows.isEmpty()
                                ? "No shows found for this criteria."
                                : shows.size() + " show(s) found.")
                        .data(shows)
                        .build());
    }

    /**
     * GET /api/shows/{id}
     * Full show detail with seat layout — for seat selection page.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowResponse>> getShowById(
            @PathVariable String id) {

        ShowResponse show = showService.getShowById(id);
        return ResponseEntity.ok(
                ApiResponse.success(show, "Show details fetched"));
    }

    /**
     * GET /api/shows/theatre/{theatreId}?date={date}
     * All shows at a specific theatre on a date.
     */
    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<ApiResponse<List<ShowSummaryResponse>>>
            getShowsByTheatre(
                @PathVariable String theatreId,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                LocalDate date) {

        LocalDate queryDate = date != null ? date : LocalDate.now();
        List<ShowSummaryResponse> shows =
                showService.getShowsByTheatreAndDate(theatreId, queryDate);

        return ResponseEntity.ok(
                ApiResponse.success(shows, "Shows fetched for theatre"));
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN
    // ─────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @Valid @RequestBody CreateShowRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        ShowResponse created = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Show created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowResponse>> updateShow(
            @PathVariable String id,
            @RequestBody UpdateShowRequest request,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        ShowResponse updated = showService.updateShow(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Show updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelShow(
            @PathVariable String id,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        showService.cancelShow(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Show cancelled successfully"));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<Page<ShowSummaryResponse>>> getAllShows(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = AppConstants.HEADER_USER_ROLES,
                           defaultValue = "") String roles) {

        validateAdmin(roles);
        Page<ShowSummaryResponse> shows =
                showService.getAllShows(page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<ShowSummaryResponse>>builder()
                        .success(true).message("All shows fetched")
                        .data(shows)
                        .page(shows.getNumber())
                        .size(shows.getSize())
                        .totalElements(shows.getTotalElements())
                        .totalPages(shows.getTotalPages())
                        .build());
    }

    // ─────────────────────────────────────────────────────────
    // INTERNAL — called by Booking Service
    // ─────────────────────────────────────────────────────────

    /**
     * PUT /api/shows/{id}/seats
     * Update booked seats after a booking is confirmed or cancelled.
     * Called by Booking Service — not exposed to frontend directly.
     */
    @PutMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<ShowResponse>> updateSeats(
            @PathVariable String id,
            @RequestParam List<String> seats,
            @RequestParam boolean isBooking) {

        ShowResponse updated =
                showService.updateSeatAvailability(id, seats, isBooking);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Seat availability updated"));
    }

    private void validateAdmin(String roles) {
        if (!roles.contains(AppConstants.ROLE_ADMIN)) {
            throw new org.springframework.security.access
                    .AccessDeniedException("Admin role required.");
        }
    }
}
