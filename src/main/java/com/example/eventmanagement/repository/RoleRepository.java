package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find role by name (e.g., "ADMIN", "SUPER_ADMIN", "ATTENDEE")
    Optional<Role> findByName(String name);

    // Check if role exists by name
    boolean existsByName(String name);
}
