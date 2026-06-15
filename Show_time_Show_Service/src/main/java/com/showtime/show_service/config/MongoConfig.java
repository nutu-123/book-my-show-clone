package com.showtime.show_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * MongoDB index configuration for Show Service.
 */
@Configuration
public class MongoConfig {

    @Bean
    public boolean createShowIndexes(MongoTemplate mongoTemplate) {
        try {
            // Shows: movie + city + date (most common query)
            mongoTemplate.indexOps("shows")
                .ensureIndex(new Index()
                    .on("movie_id",  Sort.Direction.ASC)
                    .on("city",      Sort.Direction.ASC)
                    .on("show_date", Sort.Direction.ASC)
                    .named("show_movie_city_date_idx"));

            // Shows: theatre + date
            mongoTemplate.indexOps("shows")
                .ensureIndex(new Index()
                    .on("theatre_id", Sort.Direction.ASC)
                    .on("show_date",  Sort.Direction.ASC)
                    .named("show_theatre_date_idx"));

            // Theatres: city + active
            mongoTemplate.indexOps("theatres")
                .ensureIndex(new Index()
                    .on("city",      Sort.Direction.ASC)
                    .on("is_active", Sort.Direction.ASC)
                    .named("theatre_city_active_idx"));

        } catch (Exception e) {
            // Indexes may already exist — safe to ignore
        }
        return true;
    }
}