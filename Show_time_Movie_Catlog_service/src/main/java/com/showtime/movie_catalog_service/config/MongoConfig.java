package com.showtime.movie_catalog_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * MongoDB configuration — creates indexes on startup.
 * Spring Data auto-creates @Indexed field indexes,
 * but compound and custom indexes are created here.
 */
@Configuration
public class MongoConfig {

    /**
     * Create all required indexes on startup.
     * Called automatically by Spring context.
     */
    @Bean
    public boolean createMovieIndexes(MongoTemplate mongoTemplate) {
        try {
            // Text index for full-text search
            mongoTemplate.indexOps("movies")
                .ensureIndex(new Index()
                    .on("title",       Sort.Direction.ASC)
                    .on("is_active",   Sort.Direction.ASC)
                    .named("title_active_idx"));

            // Trending score descending
            mongoTemplate.indexOps("movies")
                .ensureIndex(new Index()
                    .on("trending_score", Sort.Direction.DESC)
                    .on("is_active",      Sort.Direction.ASC)
                    .named("trending_idx"));

            // Rating descending
            mongoTemplate.indexOps("movies")
                .ensureIndex(new Index()
                    .on("rating",     Sort.Direction.DESC)
                    .on("is_active",  Sort.Direction.ASC)
                    .named("rating_idx"));

            // Review: unique per user per movie
            mongoTemplate.indexOps("reviews")
                .ensureIndex(new Index()
                    .on("movie_id", Sort.Direction.ASC)
                    .on("user_id",  Sort.Direction.ASC)
                    .named("review_unique_idx")
                    .unique());

        } catch (Exception e) {
            // Indexes already exist — safe to ignore
        }
        return true;
    }
}