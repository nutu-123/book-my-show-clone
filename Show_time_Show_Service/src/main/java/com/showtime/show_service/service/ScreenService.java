package com.showtime.show_service.service;

import com.showtime.show_service.document.Screen;
import com.showtime.show_service.dto.request.CreateScreenRequest;
import com.showtime.show_service.dto.response.ScreenResponse;
import com.showtime.show_service.exception.ResourceNotFoundException;
import com.showtime.show_service.repository.ScreenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for screen management.
 *
 * Each screen belongs to one theatre and has a
 * complete seat layout configuration.
 */
@Service
public class ScreenService {

    private static final Logger log =
            LoggerFactory.getLogger(ScreenService.class);

    @Autowired private ScreenRepository  screenRepository;
    @Autowired private TheatreService    theatreService;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    public ScreenResponse createScreen(String theatreId,
                                        CreateScreenRequest request) {
        log.info("Creating screen {} for theatre {}",
                request.getScreenName(), theatreId);

        // Verify theatre exists
        theatreService.findTheatreById(theatreId);

        // Check duplicate screen name
        if (screenRepository.existsByTheatreIdAndScreenNameIgnoreCase(
                theatreId, request.getScreenName())) {
            throw new IllegalStateException(
                    "Screen already exists: " + request.getScreenName()
                    + " in this theatre.");
        }

        // Build seat layout
        Screen.SeatLayout seatLayout = buildSeatLayout(request.getSeatLayout());

        // Calculate total seats
        int totalSeats = calculateTotalSeats(request.getSeatLayout());

        Screen screen = Screen.builder()
                .theatreId(theatreId)
                .screenName(request.getScreenName().trim())
                .screenNumber(request.getScreenNumber())
                .totalSeats(totalSeats)
                .seatLayout(seatLayout)
                .supportedFormats(request.getSupportedFormats())
                .isActive(true)
                .build();

        Screen saved = screenRepository.save(screen);

        // Update theatre's screen count
        theatreService.refreshScreenCount(theatreId);

        log.info("Screen created: {} (id={}) with {} seats",
                saved.getScreenName(), saved.getId(), saved.getTotalSeats());
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    public ScreenResponse getScreenById(String screenId) {
        return mapToResponse(findScreenById(screenId));
    }

    public List<ScreenResponse> getScreensByTheatre(String theatreId) {
        theatreService.findTheatreById(theatreId);
        return screenRepository.findByTheatreIdAndIsActiveTrue(theatreId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────

    public void deleteScreen(String screenId) {
        Screen screen = findScreenById(screenId);
        screen.setIsActive(false);
        screenRepository.save(screen);
        theatreService.refreshScreenCount(screen.getTheatreId());
        log.info("Screen deactivated: {}", screenId);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    public Screen findScreenById(String screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Screen not found: " + screenId));
    }

    private Screen.SeatLayout buildSeatLayout(
            CreateScreenRequest.SeatLayoutRequest layoutReq) {

        List<Screen.SeatCategory> categories = layoutReq.getSeatCategories()
                .stream()
                .map(c -> Screen.SeatCategory.builder()
                        .type(c.getType())
                        .rows(c.getRows())
                        .seatsPerRow(c.getSeatsPerRow())
                        .price(c.getPrice())
                        .colorCode(c.getColorCode() != null
                                ? c.getColorCode()
                                : defaultColor(c.getType()))
                        .build())
                .collect(Collectors.toList());

        return Screen.SeatLayout.builder()
                .rows(layoutReq.getRows())
                .columns(layoutReq.getColumns())
                .seatCategories(categories)
                .aisleAfterColumns(
                        layoutReq.getAisleAfterColumns() != null
                        ? layoutReq.getAisleAfterColumns()
                        : new ArrayList<>())
                .build();
    }

    private int calculateTotalSeats(
            CreateScreenRequest.SeatLayoutRequest layoutReq) {
        return layoutReq.getSeatCategories()
                .stream()
                .mapToInt(c -> c.getRows().size() * c.getSeatsPerRow())
                .sum();
    }

    private String defaultColor(String seatType) {
        return switch (seatType.toUpperCase()) {
            case "STANDARD" -> "#4CAF50";   // Green
            case "PREMIUM"  -> "#2196F3";   // Blue
            case "RECLINER" -> "#9C27B0";   // Purple
            default         -> "#607D8B";   // Grey
        };
    }

    public ScreenResponse mapToResponse(Screen s) {
        return ScreenResponse.builder()
                .id(s.getId())
                .theatreId(s.getTheatreId())
                .screenName(s.getScreenName())
                .screenNumber(s.getScreenNumber())
                .totalSeats(s.getTotalSeats())
                .seatLayout(s.getSeatLayout())
                .supportedFormats(s.getSupportedFormats())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}