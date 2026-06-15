package com.showtime.auth_service.service;

import com.showtime.auth_service.entity.RefreshToken;
import com.showtime.auth_service.entity.User;
import com.showtime.auth_service.exception.InvalidTokenException;
import com.showtime.auth_service.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing refresh tokens.
 *
 * Refresh tokens are:
 *  - UUID strings (not JWTs)
 *  - Stored in DB for revocation support
 *  - Rotated on every use (new token issued, old one deleted)
 *  - Auto-cleaned up via scheduled job
 */
@Service
public class RefreshTokenService {

    private static final Logger log =
            LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;   // ms

    /**
     * Create and save a new refresh token for a user.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000))
                .createdAt(LocalDateTime.now())
                .build();

        RefreshToken saved = refreshTokenRepository.save(token);
        log.debug("Refresh token created for user: {}", user.getEmail());
        return saved;
    }

    /**
     * Find and validate a refresh token string.
     * Throws InvalidTokenException if not found or expired.
     */
    @Transactional(readOnly = true)
    public RefreshToken findAndValidate(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token not found. Please login again."));

        if (token.isExpired()) {
            // Clean up expired token
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException(
                    "Refresh token expired. Please login again.");
        }

        return token;
    }

    /**
     * Rotate the refresh token — delete old, create new.
     * Called every time /api/auth/refresh is used.
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(user);
    }

    /**
     * Invalidate all refresh tokens for a user (logout all devices).
     */
    @Transactional
    public void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("All refresh tokens deleted for user: {}", user.getEmail());
    }

    /**
     * Scheduled cleanup — removes expired tokens daily at midnight.
     * Prevents the refresh_tokens table from growing unbounded.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Running scheduled cleanup of expired refresh tokens...");
        refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
        log.info("Expired refresh token cleanup complete");
    }
}