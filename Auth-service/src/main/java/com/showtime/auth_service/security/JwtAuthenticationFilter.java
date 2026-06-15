package com.showtime.auth_service.security;

import com.showtime.auth_service.constants.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter for Auth Service itself.
 *
 * Runs once per request (OncePerRequestFilter).
 * Extracts the JWT from the Authorization header,
 * validates it, and sets the authentication in the SecurityContext.
 *
 * Note: The API Gateway also validates JWTs.
 * This filter protects Auth Service's own secured endpoints
 * (e.g., /api/auth/me, /api/users/**).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip if no Authorization header or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(
                AppConstants.BEARER_PREFIX.length()).trim();

        try {
            final String userEmail = jwtUtil.extractUsername(jwt);

            // Only process if we have a username and no auth set yet
            if (userEmail != null
                    && SecurityContextHolder.getContext()
                                           .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Set authentication in security context
                    SecurityContextHolder.getContext()
                                        .setAuthentication(authToken);

                    log.debug("Authentication set for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.warn("JWT processing failed: {}", e.getMessage());
            // Do NOT throw — let the request continue
            // Spring Security will handle the unauthenticated state
        }

        filterChain.doFilter(request, response);
    }
}