package com.ecommerce.api.repository;

import com.ecommerce.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Extends JpaRepository to inherit all CRUD + pagination methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find user by username (used for Spring Security login) */
    Optional<User> findByUsername(String username);

    /** Find user by email */
    Optional<User> findByEmail(String email);

    /** Check if username is already taken */
    boolean existsByUsername(String username);

    /** Check if email is already registered */
    boolean existsByEmail(String email);
}
