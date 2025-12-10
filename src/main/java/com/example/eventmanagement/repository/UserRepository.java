package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find all active users
    List<User> findByActive(Boolean active);

    // Find user by username or email (for login)
    Optional<User> findByUsernameOrEmail(String username, String email);
}
