package com.showtime.show_service.constants;

/**
 * Application-wide constants for Show Service.
 */
public final class AppConstants {

    private AppConstants() {}

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER  = "ROLE_USER";

    // Header names (set by API Gateway after JWT validation)
    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    // Seat types
    public static final String SEAT_TYPE_STANDARD  = "STANDARD";
    public static final String SEAT_TYPE_PREMIUM   = "PREMIUM";
    public static final String SEAT_TYPE_RECLINER  = "RECLINER";

    // Show formats
    public static final String FORMAT_2D   = "2D";
    public static final String FORMAT_3D   = "3D";
    public static final String FORMAT_IMAX = "IMAX";
    public static final String FORMAT_4DX  = "4DX";

    // Show languages
    public static final String LANG_HINDI   = "Hindi";
    public static final String LANG_ENGLISH = "English";
    public static final String LANG_TAMIL   = "Tamil";
    public static final String LANG_TELUGU  = "Telugu";

    // Show status
    public static final String SHOW_STATUS_ACTIVE    = "ACTIVE";
    public static final String SHOW_STATUS_CANCELLED = "CANCELLED";
    public static final String SHOW_STATUS_HOUSEFULL = "HOUSEFULL";
    public static final String SHOW_STATUS_COMPLETED = "COMPLETED";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE   = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;
}