package com.showtime.auth_service.security;

import com.showtime.auth_service.entity.User;
import com.showtime.auth_service.exception.ResourceNotFoundException;
import com.showtime.auth_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation.
 *
 * Spring Security calls this during authentication to load
 * user details from the database by email (our username).
 *
 * Returns a Spring Security UserDetails object built from
 * our User entity — including roles as GrantedAuthorities.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log =
            LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by email address.
     * Called by Spring Security's DaoAuthenticationProvider.
     *
     * @param email the user's email (used as username)
     * @throws UsernameNotFoundException if user not found or disabled
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UsernameNotFoundException(
                            "User not found with email: " + email);
                });

        if (!user.getEnabled()) {
            throw new UsernameNotFoundException(
                    "User account is disabled: " + email);
        }

        // Convert our Role entities → Spring Security GrantedAuthority
        var authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        log.debug("Loaded user: {} with roles: {}", email, authorities);

        // Return Spring Security's User object (different from our User entity)
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getEnabled())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }
}