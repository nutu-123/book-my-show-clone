package com.showtime.booking_service.service;


import com.showtime.booking_service.constants.AppConstants;
import com.showtime.booking_service.exception.SeatNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Distributed Seat Locking Service using Redis.
 *
 * ═══════════════════════════════════════════════
 * LOCKING STRATEGY
 * ═══════════════════════════════════════════════
 *
 * Key:   seat:{showId}:{seatNumber}
 * Value: {userId}:{timestamp}
 * TTL:   600 seconds (10 minutes)
 *
 * Lock Operation (atomic):
 *   SET seat:show123:A1 "user42:1700000000" EX 600 NX
 *
 *   NX = Only SET if key does Not eXist
 *   EX = Expire after 600 seconds
 *
 *   If NX fails → seat is already locked by another user
 *   → return 409 CONFLICT with list of unavailable seats
 *
 * This is ATOMIC — Redis processes SET NX as single command
 * → No race conditions possible
 *
 * ═══════════════════════════════════════════════
 * FLOW
 * ═══════════════════════════════════════════════
 *
 * 1. User selects A1, A2, A3
 * 2. POST /api/bookings/lock-seats
 * 3. SeatLockService tries to lock all 3 seats atomically
 * 4a. All succeed → return lock confirmation, TTL=600s
 * 4b. Any fail → release locks already acquired → throw exception
 * 5. User has 10 min to complete payment
 * 6a. Payment success → confirm booking, release Redis locks
 *     (DB now has the confirmed booking — Redis is temp only)
 * 6b. TTL expires → Redis auto-deletes → seat available again
 * 6c. User abandons → TTL expires naturally
 */
@Service
public class SeatLockService {

    private static final Logger log =
            LoggerFactory.getLogger(SeatLockService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${booking.seat-lock-ttl-seconds:600}")
    private long seatLockTtl;

    // ─────────────────────────────────────────────────────────
    // LOCK
    // ─────────────────────────────────────────────────────────

    /**
     * Lock multiple seats for a user atomically.
     *
     * Algorithm:
     *  - Try to lock each seat using SET NX EX
     *  - If any seat fails to lock (already taken):
     *    → Roll back all locks acquired so far
     *    → Throw SeatNotAvailableException with list of failed seats
     *
     * @param showId       the show ID
     * @param seatNumbers  list of seat numbers to lock
     * @param userId       the user requesting the lock
     * @throws SeatNotAvailableException if any seat is already locked
     */
    public void lockSeats(String showId,
                          List<String> seatNumbers,
                          Long userId) {

        log.info("Attempting to lock {} seats for show {} by user {}",
                seatNumbers.size(), showId, userId);

        List<String> successfullyLocked = new ArrayList<>();
        List<String> unavailableSeats   = new ArrayList<>();

        for (String seatNumber : seatNumbers) {
            String key   = buildSeatKey(showId, seatNumber);
            String value = buildSeatValue(userId);

            // SET key value EX ttl NX (atomic)
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(key, value, Duration.ofSeconds(seatLockTtl));

            if (Boolean.TRUE.equals(locked)) {
                successfullyLocked.add(seatNumber);
                log.debug("Seat locked: {} for user {}", seatNumber, userId);
            } else {
                unavailableSeats.add(seatNumber);
                log.warn("Seat already locked: {} for show {}",
                        seatNumber, showId);
            }
        }

        // ── If any seat failed, rollback all acquired locks ──
        if (!unavailableSeats.isEmpty()) {
            log.warn("Lock failed for seats: {}. Rolling back {} locks.",
                    unavailableSeats, successfullyLocked.size());
            releaseSeats(showId, successfullyLocked, userId);

            throw new SeatNotAvailableException(
                    "Selected seats are not available: " + unavailableSeats
                    + ". Please select different seats.",
                    unavailableSeats
            );
        }

        log.info("All {} seats locked successfully for show {} by user {}",
                seatNumbers.size(), showId, userId);
    }

    // ─────────────────────────────────────────────────────────
    // RELEASE
    // ─────────────────────────────────────────────────────────

    /**
     * Release seat locks for a user.
     * Only releases if the lock belongs to this user (ownership check).
     *
     * @param showId      the show ID
     * @param seatNumbers seats to release
     * @param userId      the user releasing the lock
     */
    public void releaseSeats(String showId,
                              List<String> seatNumbers,
                              Long userId) {
        log.info("Releasing {} seats for show {} by user {}",
                seatNumbers.size(), showId, userId);

        for (String seatNumber : seatNumbers) {
            String key            = buildSeatKey(showId, seatNumber);
            String expectedValue  = buildSeatValue(userId);

            // Check ownership before deleting
            String currentValue = redisTemplate.opsForValue().get(key);

            if (expectedValue.equals(currentValue)) {
                redisTemplate.delete(key);
                log.debug("Seat lock released: {} for user {}",
                        seatNumber, userId);
            } else if (currentValue != null) {
                log.warn("Seat {} locked by different user — skipping release",
                        seatNumber);
            }
            // If null — lock already expired — nothing to do
        }
    }

    /**
     * Force-release seats regardless of ownership.
     * Used by admin or scheduled cleanup.
     */
    public void forceReleaseSeats(String showId, List<String> seatNumbers) {
        log.info("Force-releasing {} seats for show {}", seatNumbers.size(), showId);
        for (String seatNumber : seatNumbers) {
            String key = buildSeatKey(showId, seatNumber);
            redisTemplate.delete(key);
        }
    }

    // ─────────────────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────────────────

    /**
     * Verify that a user still holds locks on specific seats.
     * Called before confirming a booking.
     *
     * @return list of seats where the lock has expired or was stolen
     */
    public List<String> getExpiredOrStolenLocks(String showId,
                                                  List<String> seatNumbers,
                                                  Long userId) {
        List<String> invalid = new ArrayList<>();
        String expectedValue = buildSeatValue(userId);

        for (String seatNumber : seatNumbers) {
            String key          = buildSeatKey(showId, seatNumber);
            String currentValue = redisTemplate.opsForValue().get(key);

            if (!expectedValue.equals(currentValue)) {
                invalid.add(seatNumber);
                log.warn("Lock invalid for seat {}: expected={}, actual={}",
                        seatNumber, expectedValue, currentValue);
            }
        }
        return invalid;
    }

    /**
     * Get remaining TTL (seconds) for a seat lock.
     * Returns -2 if key does not exist (expired/not locked).
     */
    public long getLockTtl(String showId, String seatNumber) {
        String key = buildSeatKey(showId, seatNumber);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2L;
    }

    /**
     * Check if a seat is currently locked by anyone.
     */
    public boolean isSeatLocked(String showId, String seatNumber) {
        String key = buildSeatKey(showId, seatNumber);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Check if a seat is locked by a specific user.
     */
    public boolean isSeatLockedByUser(String showId, String seatNumber,
                                       Long userId) {
        String key          = buildSeatKey(showId, seatNumber);
        String currentValue = redisTemplate.opsForValue().get(key);
        return buildSeatValue(userId).equals(currentValue);
    }

    // ─────────────────────────────────────────────────────────
    // KEY BUILDERS
    // ─────────────────────────────────────────────────────────

    /**
     * Build the Redis key for a seat lock.
     * Format: seat:{showId}:{seatNumber}
     * Example: seat:64f3a9b2:A1
     */
    private String buildSeatKey(String showId, String seatNumber) {
        return AppConstants.SEAT_LOCK_KEY_PREFIX
                + showId + ":" + seatNumber;
    }

    /**
     * Build the Redis value for a seat lock.
     * Format: {userId}:{currentTimeMillis}
     * Example: 42:1700000000000
     *
     * Timestamp helps with debugging and audit.
     */
    private String buildSeatValue(Long userId) {
        return userId + ":" + System.currentTimeMillis();
    }
}