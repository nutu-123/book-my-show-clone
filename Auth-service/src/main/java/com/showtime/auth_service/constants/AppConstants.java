package com.showtime.auth_service.constants;

/**
 * Application-wide constants for Auth Service.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class — no instantiation
    }

    // Roles
    public static final String ROLE_USER  = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // JWT claim keys
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLES   = "roles";
    public static final String CLAIM_EMAIL   = "email";

    // Header names (must match API Gateway)
    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_CORRELATION = "X-Correlation-Id";

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE   = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // Token prefix
    public static final String BEARER_PREFIX = "Bearer ";
}