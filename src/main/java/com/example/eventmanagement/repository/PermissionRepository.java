package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Find permission by name
    Optional<Permission> findByPermission(String permission);

    // Check if permission exists by name
    boolean existsByPermission(String permission);
}
