package com.showtime.auth_service.service;

import com.showtime.auth_service.dto.response.UserResponse;
import com.showtime.auth_service.entity.User;
import com.showtime.auth_service.exception.ResourceNotFoundException;
import com.showtime.auth_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Service for user profile management.
 */
@Service
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Get user profile by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        return mapToResponse(user);
    }

    /**
     * Get user profile by email.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
        return mapToResponse(user);
    }

    /**
     * Update user profile.
     */
    @Transactional
    public UserResponse updateUser(Long userId, String name, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone);
        }

        User saved = userRepository.save(user);
        log.info("User updated: {}", saved.getEmail());
        return mapToResponse(saved);
    }

    /**
     * Get all users — admin only.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Map User entity to UserResponse DTO.
     * Never exposes the password field.
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.getEnabled())
                .roles(user.getRoles()
                           .stream()
                           .map(r -> r.getName())
                           .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}