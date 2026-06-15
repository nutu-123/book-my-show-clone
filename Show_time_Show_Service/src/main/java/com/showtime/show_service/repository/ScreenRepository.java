package com.showtime.show_service.repository;

import com.showtime.show_service.document.Screen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Screen documents.
 */
@Repository
public interface ScreenRepository extends MongoRepository<Screen, String> {

    // All screens for a theatre
    List<Screen> findByTheatreIdAndIsActiveTrue(String theatreId);

    // Count screens for a theatre
    long countByTheatreIdAndIsActiveTrue(String theatreId);

    // Find screen by theatre and screen number
    Optional<Screen> findByTheatreIdAndScreenNumber(
            String theatreId, Integer screenNumber);

    // Check duplicate screen name in theatre
    boolean existsByTheatreIdAndScreenNameIgnoreCase(
            String theatreId, String screenName);
}