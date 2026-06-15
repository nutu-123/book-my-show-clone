package com.showtime.auth_service.repository;

import com.showtime.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Extends JpaRepository — provides all CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address (used for login and duplicate check).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with this email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user with this phone already exists.
     */
    boolean existsByPhone(String phone);

    /**
     * Find enabled user by email — used during authentication.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Find user with roles eagerly loaded.
     * (Roles are EAGER by default, but this makes it explicit)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
}