package com.showtime.auth_service.security;

import com.showtime.auth_service.constants.AppConstants;
import com.showtime.auth_service.entity.User;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Utility — generates and validates JWT tokens.
 *
 * This is the ONLY class that generates tokens.
 * The API Gateway only validates them using the same secret.
 *
 * Token structure:
 *  Header: { "alg": "HS512" }
 *  Payload: {
 *    "sub":    "user@email.com",
 *    "userId": 42,
 *    "roles":  ["ROLE_USER"],
 *    "iat":    1700000000,
 *    "exp":    1700003600
 *  }
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Build the HMAC-SHA512 signing key from the secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ─────────────────────────────────────────────────────────
    // TOKEN GENERATION
    // ─────────────────────────────────────────────────────────

    /**
     * Generate a JWT access token for the given user.
     * Includes userId and roles as custom claims.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.CLAIM_USER_ID, user.getId());
        claims.put(AppConstants.CLAIM_EMAIL,   user.getEmail());
        claims.put(AppConstants.CLAIM_ROLES,
                user.getRoles()
                    .stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList())
        );

        return buildToken(claims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * Build the JWT string from claims, subject, and expiration.
     */
    private String buildToken(Map<String, Object> extraClaims,
                               String subject,
                               long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─────────────────────────────────────────────────────────
    // TOKEN VALIDATION
    // ─────────────────────────────────────────────────────────

    /**
     * Validate token against a UserDetails object.
     * Checks: signature, expiry, and subject match.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token structure only (no UserDetails needed).
     * Used by API Gateway.
     */
    public boolean isTokenStructureValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT empty: {}", e.getMessage());
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────
    // CLAIMS EXTRACTION
    // ─────────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get(AppConstants.CLAIM_USER_ID);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return userId != null ? Long.parseLong(userId.toString()) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get(AppConstants.CLAIM_ROLES);
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token,
                               Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Get remaining validity in milliseconds.
     */
    public long getTokenRemainingValidity(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}