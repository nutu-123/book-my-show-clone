package com.showtime.show_service.service;

import com.showtime.show_service.constants.AppConstants;
import com.showtime.show_service.document.Screen;
import com.showtime.show_service.document.Show;
import com.showtime.show_service.document.Theatre;
import com.showtime.show_service.dto.request.CreateShowRequest;
import com.showtime.show_service.dto.request.UpdateShowRequest;
import com.showtime.show_service.dto.response.ScreenResponse;
import com.showtime.show_service.dto.response.ShowResponse;
import com.showtime.show_service.dto.response.ShowSummaryResponse;
import com.showtime.show_service.exception.ResourceNotFoundException;
import com.showtime.show_service.repository.ShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for show scheduling and queries.
 *
 * Key responsibilities:
 *  - Validate no screen conflicts before creating a show
 *  - Calculate end time from movie duration
 *  - Denormalize theatre/screen/movie info for query performance
 *  - Update available seats when bookings happen (called by Booking Service)
 */
@Service
public class ShowService {

    private static final Logger log =
            LoggerFactory.getLogger(ShowService.class);

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    @Autowired private ShowRepository    showRepository;
    @Autowired private TheatreService    theatreService;
    @Autowired private ScreenService     screenService;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    /**
     * Schedule a new show.
     *
     * Steps:
     *  1. Validate theatre, screen exist and are active
     *  2. Check no conflicting shows on same screen+date+time
     *  3. Calculate end time (start + movie duration + 15 min buffer)
     *  4. Denormalize theatre/screen info
     *  5. Copy seat prices from screen layout
     *  6. Save show
     */
    public ShowResponse createShow(CreateShowRequest request) {
        log.info("Creating show for movie {} at theatre {} on {}",
                request.getMovieId(), request.getTheatreId(), request.getShowDate());

        // ── Validate theatre ──
        Theatre theatre = theatreService.findTheatreById(request.getTheatreId());
        if (!theatre.getIsActive()) {
            throw new IllegalArgumentException(
                    "Theatre is not active: " + request.getTheatreId());
        }

        // ── Validate screen ──
        Screen screen = screenService.findScreenById(request.getScreenId());
        if (!screen.getIsActive()) {
            throw new IllegalArgumentException(
                    "Screen is not active: " + request.getScreenId());
        }

        // ── Validate screen belongs to theatre ──
        if (!screen.getTheatreId().equals(request.getTheatreId())) {
            throw new IllegalArgumentException(
                    "Screen does not belong to this theatre.");
        }

        // ── Calculate end time (default 3hr movie if not provided) ──
        // In production, fetch movie duration from movie-catalog-service
        // Here we use a default 3-hour window
        LocalTime endTime = request.getStartTime().plusMinutes(195);
        // 180 min (3hr) + 15 min buffer for ads/intermission

        // ── Check for screen conflicts ──
        List<Show> conflicts = showRepository.findConflictingShows(
                request.getScreenId(),
                request.getShowDate(),
                request.getStartTime(),
                endTime
        );

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                    "Screen conflict: Another show is scheduled during this time. " +
                    "Conflicting show starts at: " +
                    conflicts.get(0).getStartTime().format(TIME_FORMATTER));
        }

        // ── Build seat prices from screen layout ──
        List<Show.SeatPriceInfo> seatPrices = buildSeatPrices(screen);

        // ── Build show document ──
        Show show = Show.builder()
                .movieId(request.getMovieId())
                .movieTitle(request.getMovieTitle() != null
                        ? request.getMovieTitle() : "")
                .moviePosterUrl(request.getMoviePosterUrl())
                .theatreId(request.getTheatreId())
                .theatreName(theatre.getName())
                .screenId(request.getScreenId())
                .screenName(screen.getScreenName())
                .city(theatre.getCity())
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .language(request.getLanguage())
                .format(request.getFormat())
                .totalSeats(screen.getTotalSeats())
                .availableSeats(screen.getTotalSeats())
                .bookedSeats(new ArrayList<>())
                .seatPrices(seatPrices)
                .status(AppConstants.SHOW_STATUS_ACTIVE)
                .isActive(true)
                .build();

        Show saved = showRepository.save(show);
        log.info("Show created: {} (id={})", saved.getId(), saved.getId());
        return mapToFullResponse(saved, screen);
    }

    // ─────────────────────────────────────────────────────────
    // READ — SINGLE
    // ─────────────────────────────────────────────────────────

    /**
     * Get full show details including screen layout.
     * Used by the seat selection page.
     */
    public ShowResponse getShowById(String showId) {
        Show show = findShowById(showId);
        Screen screen = screenService.findScreenById(show.getScreenId());
        return mapToFullResponse(show, screen);
    }

    // ─────────────────────────────────────────────────────────
    // READ — LISTINGS
    // ─────────────────────────────────────────────────────────

    /**
     * Get all shows for a movie in a city on a date.
     * This is the primary query for the Theatre Selection page.
     *
     * Groups by theatre for frontend display.
     */
    public List<ShowSummaryResponse> getShowsByMovieCityDate(
            String movieId, String city, LocalDate date) {

        log.debug("Fetching shows: movie={}, city={}, date={}",
                movieId, city, date);

        List<Show> shows = showRepository
                .findByMovieIdAndCityIgnoreCaseAndShowDateAndIsActiveTrue(
                        movieId, city, date);

        return shows.stream()
                .map(this::mapToSummaryResponse)
                .sorted((a, b) -> {
                    // Sort by theatre name, then by start time
                    int theatreCompare = a.getTheatreName()
                            .compareTo(b.getTheatreName());
                    if (theatreCompare != 0) return theatreCompare;
                    return a.getStartTime().compareTo(b.getStartTime());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get shows at a specific theatre on a date.
     */
    public List<ShowSummaryResponse> getShowsByTheatreAndDate(
            String theatreId, LocalDate date) {

        return showRepository
                .findByTheatreIdAndShowDateAndIsActiveTrue(theatreId, date)
                .stream()
                .map(this::mapToSummaryResponse)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    /**
     * Get all shows (admin — paginated).
     */
    public Page<ShowSummaryResponse> getAllShows(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "showDate"));
        return showRepository.findByIsActiveTrue(pageable)
                .map(this::mapToSummaryResponse);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────

    public ShowResponse updateShow(String showId, UpdateShowRequest request) {
        Show show = findShowById(showId);

        if (request.getShowDate()  != null) show.setShowDate(request.getShowDate());
        if (request.getStartTime() != null) show.setStartTime(request.getStartTime());
        if (request.getLanguage()  != null) show.setLanguage(request.getLanguage());
        if (request.getFormat()    != null) show.setFormat(request.getFormat());
        if (request.getStatus()    != null) show.setStatus(request.getStatus());
        if (request.getIsActive()  != null) show.setIsActive(request.getIsActive());

        Show saved = showRepository.save(show);
        Screen screen = screenService.findScreenById(saved.getScreenId());
        return mapToFullResponse(saved, screen);
    }

    /**
     * Update available seats — called by Booking Service
     * when seats are confirmed booked or booking is cancelled.
     */
    public ShowResponse updateSeatAvailability(String showId,
                                                List<String> seatsToBook,
                                                boolean isBooking) {
        Show show = findShowById(showId);

        if (isBooking) {
            // Add to booked seats
            show.getBookedSeats().addAll(seatsToBook);
            show.setAvailableSeats(
                    show.getAvailableSeats() - seatsToBook.size());

            // Mark as housefull if no seats left
            if (show.getAvailableSeats() <= 0) {
                show.setStatus(AppConstants.SHOW_STATUS_HOUSEFULL);
            }
        } else {
            // Remove from booked seats (cancellation)
            show.getBookedSeats().removeAll(seatsToBook);
            show.setAvailableSeats(
                    show.getAvailableSeats() + seatsToBook.size());

            // Restore to ACTIVE if was housefull
            if (AppConstants.SHOW_STATUS_HOUSEFULL.equals(show.getStatus())) {
                show.setStatus(AppConstants.SHOW_STATUS_ACTIVE);
            }
        }

        Show saved = showRepository.save(show);
        Screen screen = screenService.findScreenById(saved.getScreenId());
        return mapToFullResponse(saved, screen);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────

    public void cancelShow(String showId) {
        Show show = findShowById(showId);

        if (!show.getBookedSeats().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot cancel show with existing bookings. " +
                    "Please process refunds first.");
        }

        show.setStatus(AppConstants.SHOW_STATUS_CANCELLED);
        show.setIsActive(false);
        showRepository.save(show);
        log.info("Show cancelled: {}", showId);
    }

    // ─────────────────────────────────────────────────────────
    // SEAT AVAILABILITY CHECK
    // ─────────────────────────────────────────────────────────

    /**
     * Check if specific seats are available in a show.
     * Called by Booking Service before locking seats.
     */
    public boolean areSeatsAvailable(String showId,
                                      List<String> requestedSeats) {
        Show show = findShowById(showId);

        if (!AppConstants.SHOW_STATUS_ACTIVE.equals(show.getStatus())) {
            return false;
        }

        // Check none of the requested seats are already booked
        for (String seat : requestedSeats) {
            if (show.isSeatBooked(seat)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get seat price for a specific seat in a show.
     * Determines price based on which row the seat belongs to.
     */
    public double getSeatPrice(String showId, String seatNumber) {
        Show show = findShowById(showId);
        Screen screen = screenService.findScreenById(show.getScreenId());

        // Extract row label from seat number (e.g., "A" from "A1", "B" from "B12")
        String rowLabel = seatNumber.replaceAll("[0-9]", "");

        if (screen.getSeatLayout() == null
                || screen.getSeatLayout().getSeatCategories() == null) {
            return 200.0; // default price
        }

        return screen.getSeatLayout().getSeatCategories()
                .stream()
                .filter(cat -> cat.getRows().contains(rowLabel))
                .findFirst()
                .map(Screen.SeatCategory::getPrice)
                .orElse(200.0); // default
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    public Show findShowById(String showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found: " + showId));
    }

    private List<Show.SeatPriceInfo> buildSeatPrices(Screen screen) {
        if (screen.getSeatLayout() == null
                || screen.getSeatLayout().getSeatCategories() == null) {
            return new ArrayList<>();
        }

        return screen.getSeatLayout().getSeatCategories()
                .stream()
                .map(cat -> Show.SeatPriceInfo.builder()
                        .seatType(cat.getType())
                        .price(cat.getPrice())
                        .colorCode(cat.getColorCode())
                        .build())
                .collect(Collectors.toList());
    }

    public ShowResponse mapToFullResponse(Show show, Screen screen) {
        return ShowResponse.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .movieTitle(show.getMovieTitle())
                .moviePosterUrl(show.getMoviePosterUrl())
                .theatreId(show.getTheatreId())
                .theatreName(show.getTheatreName())
                .screenId(show.getScreenId())
                .screenName(show.getScreenName())
                .city(show.getCity())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .language(show.getLanguage())
                .format(show.getFormat())
                .totalSeats(show.getTotalSeats())
                .availableSeats(show.getAvailableSeats())
                .bookedSeats(show.getBookedSeats())
                .seatPrices(show.getSeatPrices())
                .status(show.getStatus())
                .isActive(show.getIsActive())
                .screen(screenService.mapToResponse(screen))
                .createdAt(show.getCreatedAt())
                .build();
    }

    public ShowSummaryResponse mapToSummaryResponse(Show show) {
        return ShowSummaryResponse.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .theatreId(show.getTheatreId())
                .theatreName(show.getTheatreName())
                .screenId(show.getScreenId())
                .screenName(show.getScreenName())
                .city(show.getCity())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .language(show.getLanguage())
                .format(show.getFormat())
                .totalSeats(show.getTotalSeats())
                .availableSeats(show.getAvailableSeats())
                .status(show.getStatus())
                .seatPrices(show.getSeatPrices())
                .isAvailable(show.getAvailableSeats() != null
                        && show.getAvailableSeats() > 0)
                .startTimeFormatted(show.getStartTime() != null
                        ? show.getStartTime().format(TIME_FORMATTER) : "")
                .build();
    }
}