package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.RoleDTO;
import com.example.eventmanagement.dto.RolePermissionDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.RoleMapper;
import com.example.eventmanagement.model.Permission;
import com.example.eventmanagement.model.Role;
import com.example.eventmanagement.repository.PermissionRepository;
import com.example.eventmanagement.repository.RoleRepository;
import com.example.eventmanagement.util.ApplicationLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final ActivityHistoryService activityHistoryService;
    private final ApplicationLogger applicationLogger;
    private final Logger logger;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RoleMapper roleMapper,
                       ActivityHistoryService activityHistoryService,
                       ApplicationLogger applicationLogger) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
        this.activityHistoryService = activityHistoryService;
        this.applicationLogger = applicationLogger;
        this.logger = applicationLogger.getLogger(RoleService.class);
    }

    // Create new role
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "CREATE", "Role", roleDTO.getName());
            logger.info("Creating new role with name: {}", roleDTO.getName());

            // Check if role name already exists
            if (roleRepository.existsByName(roleDTO.getName())) {
                logger.warn("Role creation failed: Role name '{}' already exists", roleDTO.getName());
                throw new IllegalArgumentException("Role with name '" + roleDTO.getName() + "' already exists");
            }

            // Convert DTO to Entity
            Role role = roleMapper.toEntity(roleDTO);

            // Save to database
            Role savedRole = roleRepository.save(role);
            logger.info("Role created successfully with ID: {}", savedRole.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.ROLE_CREATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "CREATE", "Role", savedRole.getId());

            // Convert back to DTO and return
            return roleMapper.toDTO(savedRole);

        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Role creation failed: Validation error", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Role creation failed: Unexpected error", e);
            throw new RuntimeException("Failed to create role: " + e.getMessage(), e);
        }
    }

    // Get all roles (without permissions)
    public List<RoleDTO> getAllRoles() {
        try {
            logger.debug("Fetching all roles without permissions");
            List<Role> roles = roleRepository.findAll();
            logger.debug("Found {} roles", roles.size());

            return roles.stream()
                    .map(roleMapper::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch all roles", e);
            throw new RuntimeException("Failed to fetch roles: " + e.getMessage(), e);
        }
    }

    // Get all roles (with permissions)
    public List<RoleDTO> getAllRolesWithPermissions() {
        try {
            logger.debug("Fetching all roles with permissions");
            List<Role> roles = roleRepository.findAll();
            logger.debug("Found {} roles with permissions", roles.size());

            return roles.stream()
                    .map(roleMapper::toDTOWithPermissions)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch all roles with permissions", e);
            throw new RuntimeException("Failed to fetch roles: " + e.getMessage(), e);
        }
    }

    // Get role by ID (without permissions)
    public RoleDTO getRoleById(Long id) {
        try {
            logger.debug("Fetching role with ID: {}", id);
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

            logger.debug("Found role: {} (ID: {})", role.getName(), role.getId());
            return roleMapper.toDTO(role);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Role not found with ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch role ID: " + id, e);
            throw new RuntimeException("Failed to fetch role: " + e.getMessage(), e);
        }
    }

    // Get role by ID (with permissions)
    public RoleDTO getRoleByIdWithPermissions(Long id) {
        try {
            logger.debug("Fetching role with permissions for ID: {}", id);
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

            logger.debug("Found role with permissions: {} (ID: {})", role.getName(), role.getId());
            return roleMapper.toDTOWithPermissions(role);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Role not found with ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch role with permissions ID: " + id, e);
            throw new RuntimeException("Failed to fetch role: " + e.getMessage(), e);
        }
    }

    // Get role by name
    public RoleDTO getRoleByName(String name) {
        try {
            logger.debug("Fetching role with name: {}", name);
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with name: " + name));

            logger.debug("Found role: {} (ID: {})", role.getName(), role.getId());
            return roleMapper.toDTO(role);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Role not found with name: " + name, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch role by name", e);
            throw new RuntimeException("Failed to fetch role: " + e.getMessage(), e);
        }
    }

    // Update role
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "UPDATE", "Role", id);
            logger.info("Updating role with ID: {}", id);

            // Find existing role
            Role existingRole = roleRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

            logger.debug("Found role to update: {} (ID: {})", existingRole.getName(), existingRole.getId());

            // Check if new name already exists (and it's not the current role)
            if (!existingRole.getName().equals(roleDTO.getName()) &&
                    roleRepository.existsByName(roleDTO.getName())) {
                logger.warn("Role update failed: Role name '{}' already exists", roleDTO.getName());
                throw new IllegalArgumentException("Role with name '" + roleDTO.getName() + "' already exists");
            }

            // Update entity from DTO
            roleMapper.updateEntityFromDTO(roleDTO, existingRole);

            // Save updated entity
            Role updatedRole = roleRepository.save(existingRole);
            logger.info("Role updated successfully with ID: {}", updatedRole.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.ROLE_UPDATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "UPDATE", "Role", updatedRole.getId());

            // Convert to DTO and return
            return roleMapper.toDTO(updatedRole);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Role update failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Role update failed: Validation error for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Role update failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to update role: " + e.getMessage(), e);
        }
    }

    // Delete role
    @Transactional
    public void deleteRole(Long id, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "DELETE", "Role", id);
            logger.info("Deleting role with ID: {}", id);

            // Check if role exists
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

            logger.debug("Found role to delete: {} (ID: {})", role.getName(), role.getId());

            // Check if role has users (prevent deletion if users exist)
            if (!role.getUsers().isEmpty()) {
                logger.warn("Role deletion failed: Role ID {} has {} assigned users", id, role.getUsers().size());
                throw new IllegalStateException("Cannot delete role. " + role.getUsers().size() + " user(s) are assigned to this role");
            }

            // Delete role
            roleRepository.delete(role);
            logger.info("Role deleted successfully with ID: {}", id);

            // Record activity
            activityHistoryService.recordActivity(ActivityType.ROLE_DELETE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "DELETE", "Role", id);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Role deletion failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (IllegalStateException e) {
            applicationLogger.logError(logger, "Role deletion failed: State validation error for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Role deletion failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to delete role: " + e.getMessage(), e);
        }
    }

    // ===== Permission Management Methods =====

    // Assign permissions to a role
    @Transactional
    public RoleDTO assignPermissionsToRole(RolePermissionDTO rolePermissionDTO, HttpServletRequest request) {
        try {
            logger.info("Assigning {} permissions to role ID: {}",
                    rolePermissionDTO.getPermissionIds().size(), rolePermissionDTO.getRoleId());

            // Find role
            Role role = roleRepository.findById(rolePermissionDTO.getRoleId())
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + rolePermissionDTO.getRoleId()));

            logger.debug("Found role: {}", role.getName());

            // Find all permissions by IDs
            Set<Permission> permissions = new HashSet<>();
            for (Long permissionId : rolePermissionDTO.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + permissionId));
                permissions.add(permission);
                logger.debug("Added permission: {} to role: {}", permission.getPermission(), role.getName());
            }

            // Set permissions to role (replaces existing permissions)
            role.setPermissions(permissions);

            // Save role
            Role savedRole = roleRepository.save(role);
            logger.info("Successfully assigned {} permissions to role: {}", permissions.size(), savedRole.getName());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.ROLE_ASSIGN_PERMISSION, request);

            // Return role with permissions
            return roleMapper.toDTOWithPermissions(savedRole);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Failed to assign permissions: Entity not found", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to assign permissions to role", e);
            throw new RuntimeException("Failed to assign permissions: " + e.getMessage(), e);
        }
    }

    // Add single permission to role
    @Transactional
    public RoleDTO addPermissionToRole(Long roleId, Long permissionId, HttpServletRequest request) {
        try {
            logger.info("Adding permission ID {} to role ID {}", permissionId, roleId);

            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + roleId));

            logger.debug("Found role: {}", role.getName());

            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + permissionId));

            logger.debug("Found permission: {}", permission.getPermission());

            // Add permission to role
            role.addPermission(permission);

            // Save role
            Role savedRole = roleRepository.save(role);
            logger.info("Successfully added permission '{}' to role '{}'", permission.getPermission(), savedRole.getName());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.ROLE_ASSIGN_PERMISSION, request);

            // Return role with permissions
            return roleMapper.toDTOWithPermissions(savedRole);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Failed to add permission: Entity not found", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to add permission to role", e);
            throw new RuntimeException("Failed to add permission: " + e.getMessage(), e);
        }
    }

    // Remove permission from role
    @Transactional
    public RoleDTO removePermissionFromRole(Long roleId, Long permissionId, HttpServletRequest request) {
        try {
            logger.info("Removing permission ID {} from role ID {}", permissionId, roleId);

            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + roleId));

            logger.debug("Found role: {}", role.getName());

            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + permissionId));

            logger.debug("Found permission: {}", permission.getPermission());

            // Remove permission from role
            role.removePermission(permission);

            // Save role
            Role savedRole = roleRepository.save(role);
            logger.info("Successfully removed permission '{}' from role '{}'", permission.getPermission(), savedRole.getName());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.ROLE_REMOVE_PERMISSION, request);

            // Return role with permissions
            return roleMapper.toDTOWithPermissions(savedRole);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Failed to remove permission: Entity not found", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to remove permission from role", e);
            throw new RuntimeException("Failed to remove permission: " + e.getMessage(), e);
        }
    }
}
