package com.showtime.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT Utility for API Gateway.
 *
 * Only VALIDATES tokens — does not generate them.
 * Token generation is Auth Service's responsibility.
 *
 * The JWT secret MUST be identical to the one in auth-service.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Build the signing key from the secret string.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validate the JWT token.
     * Returns true if valid, false otherwise.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract all claims from the token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the username (subject) from the token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract the user ID from the token.
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        return userId != null ? userId.toString() : null;
    }

    /**
     * Extract roles from the token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    /**
     * Check if the token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}