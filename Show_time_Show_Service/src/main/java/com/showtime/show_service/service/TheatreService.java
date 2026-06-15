package com.showtime.show_service.service;

import com.showtime.show_service.constants.AppConstants;
import com.showtime.show_service.document.Theatre;
import com.showtime.show_service.dto.request.CreateTheatreRequest;
import com.showtime.show_service.dto.request.UpdateTheatreRequest;
import com.showtime.show_service.dto.response.TheatreResponse;
import com.showtime.show_service.exception.ResourceNotFoundException;
import com.showtime.show_service.repository.ScreenRepository;
import com.showtime.show_service.repository.TheatreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for theatre management.
 */
@Service
public class TheatreService {

    private static final Logger log =
            LoggerFactory.getLogger(TheatreService.class);

    @Autowired private TheatreRepository theatreRepository;
    @Autowired private ScreenRepository  screenRepository;
    @Autowired private MongoTemplate     mongoTemplate;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    public TheatreResponse createTheatre(CreateTheatreRequest request) {
        log.info("Creating theatre: {} in {}", request.getName(), request.getCity());

        if (theatreRepository.existsByNameIgnoreCaseAndCityIgnoreCase(
                request.getName(), request.getCity())) {
            throw new IllegalStateException(
                    "Theatre already exists: " + request.getName()
                    + " in " + request.getCity());
        }

        Theatre theatre = Theatre.builder()
                .name(request.getName().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .address(request.getAddress().trim())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .amenities(request.getAmenities())
                .totalScreens(0)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .googleMapsUrl(request.getGoogleMapsUrl())
                .isActive(true)
                .build();

        Theatre saved = theatreRepository.save(theatre);
        log.info("Theatre created: {} (id={})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    public TheatreResponse getTheatreById(String theatreId) {
        return mapToResponse(findTheatreById(theatreId));
    }

    public Page<TheatreResponse> getAllTheatres(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "city", "name"));
        return theatreRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    public Page<TheatreResponse> getTheatresByCity(
            String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "name"));
        return theatreRepository
                .findByCityIgnoreCaseAndIsActiveTrue(city, pageable)
                .map(this::mapToResponse);
    }

    public List<TheatreResponse> getAllTheatresInCity(String city) {
        return theatreRepository
                .findByCityIgnoreCaseAndIsActiveTrueOrderByNameAsc(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct cities that have active theatres.
     */
    public List<String> getAllCities() {
        return mongoTemplate.findDistinct(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria
                                .where("is_active").is(true)
                ),
                "city",
                Theatre.class,
                String.class
        );
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────

    public TheatreResponse updateTheatre(String theatreId,
                                          UpdateTheatreRequest request) {
        Theatre theatre = findTheatreById(theatreId);

        if (request.getName()         != null) theatre.setName(request.getName());
        if (request.getCity()         != null) theatre.setCity(request.getCity());
        if (request.getState()        != null) theatre.setState(request.getState());
        if (request.getAddress()      != null) theatre.setAddress(request.getAddress());
        if (request.getPincode()      != null) theatre.setPincode(request.getPincode());
        if (request.getPhone()        != null) theatre.setPhone(request.getPhone());
        if (request.getEmail()        != null) theatre.setEmail(request.getEmail());
        if (request.getAmenities()    != null) theatre.setAmenities(request.getAmenities());
        if (request.getLatitude()     != null) theatre.setLatitude(request.getLatitude());
        if (request.getLongitude()    != null) theatre.setLongitude(request.getLongitude());
        if (request.getGoogleMapsUrl()!= null) theatre.setGoogleMapsUrl(request.getGoogleMapsUrl());
        if (request.getIsActive()     != null) theatre.setIsActive(request.getIsActive());

        Theatre saved = theatreRepository.save(theatre);
        log.info("Theatre updated: {}", theatreId);
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────

    public void deleteTheatre(String theatreId) {
        Theatre theatre = findTheatreById(theatreId);
        theatre.setIsActive(false);
        theatreRepository.save(theatre);
        log.info("Theatre deactivated: {}", theatreId);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    public Theatre findTheatreById(String theatreId) {
        return theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Theatre not found: " + theatreId));
    }

    /**
     * Refresh total screen count for a theatre.
     * Called by ScreenService after adding/removing screens.
     */
    public void refreshScreenCount(String theatreId) {
        Theatre theatre = findTheatreById(theatreId);
        long count = screenRepository.countByTheatreIdAndIsActiveTrue(theatreId);
        theatre.setTotalScreens((int) count);
        theatreRepository.save(theatre);
    }

    public TheatreResponse mapToResponse(Theatre t) {
        return TheatreResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .city(t.getCity())
                .state(t.getState())
                .address(t.getAddress())
                .pincode(t.getPincode())
                .phone(t.getPhone())
                .email(t.getEmail())
                .amenities(t.getAmenities())
                .totalScreens(t.getTotalScreens())
                .isActive(t.getIsActive())
                .latitude(t.getLatitude())
                .longitude(t.getLongitude())
                .googleMapsUrl(t.getGoogleMapsUrl())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}