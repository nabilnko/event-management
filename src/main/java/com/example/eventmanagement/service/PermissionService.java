package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.PermissionDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.PermissionMapper;
import com.example.eventmanagement.model.Permission;
import com.example.eventmanagement.repository.PermissionRepository;
import com.example.eventmanagement.util.ApplicationLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final ActivityHistoryService activityHistoryService;
    private final ApplicationLogger applicationLogger;
    private final Logger logger;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository,
                             PermissionMapper permissionMapper,
                             ActivityHistoryService activityHistoryService,
                             ApplicationLogger applicationLogger) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.activityHistoryService = activityHistoryService;
        this.applicationLogger = applicationLogger;
        this.logger = applicationLogger.getLogger(PermissionService.class);
    }

    // Create new permission
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "CREATE", "Permission", permissionDTO.getPermission());
            logger.info("Creating new permission: {}", permissionDTO.getPermission());

            // Check if permission already exists
            if (permissionRepository.existsByPermission(permissionDTO.getPermission())) {
                logger.warn("Permission creation failed: Permission '{}' already exists", permissionDTO.getPermission());
                throw new IllegalArgumentException("Permission '" + permissionDTO.getPermission() + "' already exists");
            }

            // Convert DTO to Entity
            Permission permission = permissionMapper.toEntity(permissionDTO);

            // Save to database
            Permission savedPermission = permissionRepository.save(permission);
            logger.info("Permission created successfully with ID: {}", savedPermission.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.PERMISSION_CREATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "CREATE", "Permission", savedPermission.getId());

            // Convert back to DTO and return
            return permissionMapper.toDTO(savedPermission);

        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Permission creation failed: Validation error", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Permission creation failed: Unexpected error", e);
            throw new RuntimeException("Failed to create permission: " + e.getMessage(), e);
        }
    }

    // Get all permissions
    public List<PermissionDTO> getAllPermissions() {
        try {
            logger.debug("Fetching all permissions");
            List<Permission> permissions = permissionRepository.findAll();
            logger.debug("Found {} permissions", permissions.size());
            return permissionMapper.toDTOList(permissions);

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch all permissions", e);
            throw new RuntimeException("Failed to fetch permissions: " + e.getMessage(), e);
        }
    }

    // Get permission by ID
    public PermissionDTO getPermissionById(Long id) {
        try {
            logger.debug("Fetching permission with ID: {}", id);
            Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + id));

            logger.debug("Found permission: {} (ID: {})", permission.getPermission(), permission.getId());
            return permissionMapper.toDTO(permission);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Permission not found with ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch permission ID: " + id, e);
            throw new RuntimeException("Failed to fetch permission: " + e.getMessage(), e);
        }
    }

    // Get permission by name
    public PermissionDTO getPermissionByName(String permissionName) {
        try {
            logger.debug("Fetching permission with name: {}", permissionName);
            Permission permission = permissionRepository.findByPermission(permissionName)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with name: " + permissionName));

            logger.debug("Found permission: {} (ID: {})", permission.getPermission(), permission.getId());
            return permissionMapper.toDTO(permission);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Permission not found: " + permissionName, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch permission by name", e);
            throw new RuntimeException("Failed to fetch permission: " + e.getMessage(), e);
        }
    }

    // Update permission
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "UPDATE", "Permission", id);
            logger.info("Updating permission with ID: {}", id);

            // Find existing permission
            Permission existingPermission = permissionRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + id));

            logger.debug("Found permission to update: {} (ID: {})", existingPermission.getPermission(), existingPermission.getId());

            // Check if new name already exists (and it's not the current permission)
            if (!existingPermission.getPermission().equals(permissionDTO.getPermission()) &&
                    permissionRepository.existsByPermission(permissionDTO.getPermission())) {
                logger.warn("Permission update failed: Permission '{}' already exists", permissionDTO.getPermission());
                throw new IllegalArgumentException("Permission '" + permissionDTO.getPermission() + "' already exists");
            }

            // Update entity from DTO
            permissionMapper.updateEntityFromDTO(permissionDTO, existingPermission);

            // Save updated permission
            Permission updatedPermission = permissionRepository.save(existingPermission);
            logger.info("Permission updated successfully with ID: {}", updatedPermission.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.PERMISSION_UPDATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "UPDATE", "Permission", updatedPermission.getId());

            // Convert to DTO and return
            return permissionMapper.toDTO(updatedPermission);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Permission update failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Permission update failed: Validation error for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Permission update failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to update permission: " + e.getMessage(), e);
        }
    }

    // Delete permission
    @Transactional
    public void deletePermission(Long id, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "DELETE", "Permission", id);
            logger.info("Deleting permission with ID: {}", id);

            // Check if permission exists
            Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + id));

            logger.debug("Found permission to delete: {} (ID: {})", permission.getPermission(), permission.getId());

            // Delete permission
            permissionRepository.delete(permission);
            logger.info("Permission deleted successfully with ID: {}", id);

            // Record activity
            activityHistoryService.recordActivity(ActivityType.PERMISSION_DELETE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "DELETE", "Permission", id);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Permission deletion failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Permission deletion failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to delete permission: " + e.getMessage(), e);
        }
    }
}
