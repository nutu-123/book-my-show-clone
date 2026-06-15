package com.showtime.auth_service.service;


import com.showtime.auth_service.constants.AppConstants;
import com.showtime.auth_service.dto.request.LoginRequest;
import com.showtime.auth_service.dto.request.RegisterRequest;
import com.showtime.auth_service.dto.request.RefreshTokenRequest;
import com.showtime.auth_service.dto.response.AuthResponse;
import com.showtime.auth_service.dto.response.UserResponse;
import com.showtime.auth_service.entity.RefreshToken;
import com.showtime.auth_service.entity.Role;
import com.showtime.auth_service.entity.User;
import com.showtime.auth_service.exception.ResourceNotFoundException;
import com.showtime.auth_service.exception.UserAlreadyExistsException;
import com.showtime.auth_service.repository.RoleRepository;
import com.showtime.auth_service.repository.UserRepository;
import com.showtime.auth_service.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Core authentication service.
 *
 * Handles:
 *  - User registration (with role assignment)
 *  - Login (JWT generation)
 *  - Token refresh (rotation)
 *  - Logout (token invalidation)
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository         userRepository;
    @Autowired private RoleRepository         roleRepository;
    @Autowired private PasswordEncoder        passwordEncoder;
    @Autowired private JwtUtil                jwtUtil;
    @Autowired private AuthenticationManager  authenticationManager;
    @Autowired private RefreshTokenService    refreshTokenService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // ─────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────

    /**
     * Register a new user with ROLE_USER by default.
     *
     * Steps:
     *  1. Check duplicate email/phone
     *  2. Encode password
     *  3. Assign default role
     *  4. Save user
     *  5. Generate tokens
     *  6. Return AuthResponse
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // ── Check duplicates ──
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " + request.getEmail());
        }

        if (request.getPhone() != null
                && userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException(
                    "Phone number already registered: " + request.getPhone());
        }

        // ── Fetch default role ──
        Role userRole = roleRepository.findByName(AppConstants.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default role not found. Run data.sql seeder."));

        // ── Build user entity ──
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .build();

        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} (id={})",
                savedUser.getEmail(), savedUser.getId());

        // ── Generate tokens ──
        return buildAuthResponse(savedUser);
    }

    // ─────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────

    /**
     * Authenticate user and return JWT tokens.
     *
     * Uses Spring Security's AuthenticationManager to validate
     * credentials — throws BadCredentialsException on failure.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        // ── Authenticate via Spring Security ──
        // This calls CustomUserDetailsService + BCrypt comparison
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        // ── Load full user entity ──
        User user = userRepository
                .findActiveUserByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        log.info("User authenticated: {} (id={})",
                user.getEmail(), user.getId());

        return buildAuthResponse(user);
    }

    // ─────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────

    /**
     * Refresh access token using a valid refresh token.
     * Old refresh token is invalidated; new one is issued (rotation).
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refresh token request received");

        // ── Validate refresh token ──
        RefreshToken oldToken = refreshTokenService
                .findAndValidate(request.getRefreshToken());

        User user = oldToken.getUser();

        // ── Rotate refresh token ──
        RefreshToken newRefreshToken =
                refreshTokenService.rotateRefreshToken(oldToken);

        // ── Generate new access token ──
        String newAccessToken = jwtUtil.generateAccessToken(user);

        log.info("Token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles()
                           .stream()
                           .map(Role::getName)
                           .collect(Collectors.toList()))
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────

    /**
     * Logout — invalidates all refresh tokens for the user.
     * Access token will expire naturally (short-lived: 1 hour).
     */
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));

        refreshTokenService.deleteAllUserTokens(user);
        log.info("User logged out: {} (id={})", user.getEmail(), userId);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Build the AuthResponse with both tokens.
     * Called after register and login.
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(roles)
                .build();
    }
}