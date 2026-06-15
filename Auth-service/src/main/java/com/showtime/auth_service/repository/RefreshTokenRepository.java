package com.showtime.auth_service.repository;

import com.showtime.auth_service.entity.RefreshToken;
import com.showtime.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its string value.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Delete all refresh tokens for a specific user.
     * Used for logout (invalidate all sessions).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);

    /**
     * Delete all expired refresh tokens (cleanup job).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count active tokens for a user (session management).
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user")
    long countByUser(@Param("user") User user);
}