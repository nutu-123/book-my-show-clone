package com.showtime.movie_catalog_service.constants;


/**
 * Application-wide constants for Movie Catalog Service.
 */
public final class AppConstants {

    private AppConstants() {}

    // Pagination
    public static final int    DEFAULT_PAGE_SIZE   = 10;
    public static final int    DEFAULT_PAGE_NUMBER = 0;
    public static final int    MAX_PAGE_SIZE       = 50;

    // Movie status
    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // Languages
    public static final String LANG_HINDI   = "Hindi";
    public static final String LANG_ENGLISH = "English";
    public static final String LANG_TAMIL   = "Tamil";
    public static final String LANG_TELUGU  = "Telugu";

    // Formats
    public static final String FORMAT_2D   = "2D";
    public static final String FORMAT_3D   = "3D";
    public static final String FORMAT_IMAX = "IMAX";

    // Header names (set by API Gateway)
    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER  = "ROLE_USER";

    // Trending window (days)
    public static final int TRENDING_DAYS = 7;

    // Max rating
    public static final double MAX_RATING = 5.0;
    public static final double MIN_RATING = 1.0;
}