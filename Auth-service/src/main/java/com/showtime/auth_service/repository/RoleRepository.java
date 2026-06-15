package com.showtime.auth_service.repository;

import com.showtime.auth_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name (e.g., "ROLE_USER", "ROLE_ADMIN").
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role with this name exists.
     */
    boolean existsByName(String name);
}