package com.showtime.show_service.repository;


import com.showtime.show_service.document.Theatre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Theatre documents.
 */
@Repository
public interface TheatreRepository extends MongoRepository<Theatre, String> {

    // Active theatres
    Page<Theatre> findByIsActiveTrue(Pageable pageable);

    // By city (case-insensitive)
    Page<Theatre> findByCityIgnoreCaseAndIsActiveTrue(
            String city, Pageable pageable);

    // List all theatres in a city
    List<Theatre> findByCityIgnoreCaseAndIsActiveTrueOrderByNameAsc(
            String city);

    // Check name + city duplicate
    boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);

    // Count by city
    long countByCityIgnoreCaseAndIsActiveTrue(String city);

    // Get all active cities
    // (distinct query done via MongoTemplate in service)
}