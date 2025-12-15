package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.RoleDTO;
import com.example.eventmanagement.dto.RolePermissionDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.RoleMapper;
import com.example.eventmanagement.model.Permission;
import com.example.eventmanagement.model.Role;
import com.example.eventmanagement.repository.PermissionRepository;
import com.example.eventmanagement.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RoleMapper roleMapper,
                       ActivityHistoryService activityHistoryService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
        this.activityHistoryService = activityHistoryService;
    }

    // Create new role
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO, HttpServletRequest request) {
        // Check if role name already exists
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() + "' already exists");
        }

        // Convert DTO to Entity
        Role role = roleMapper.toEntity(roleDTO);

        // Save to database
        Role savedRole = roleRepository.save(role);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.ROLE_CREATE, request);

        // Convert back to DTO and return
        return roleMapper.toDTO(savedRole);
    }

    // Get all roles (without permissions)
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get all roles (with permissions)
    public List<RoleDTO> getAllRolesWithPermissions() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(roleMapper::toDTOWithPermissions)
                .collect(Collectors.toList());
    }

    // Get role by ID (without permissions)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

        return roleMapper.toDTO(role);
    }

    // Get role by ID (with permissions)
    public RoleDTO getRoleByIdWithPermissions(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

        return roleMapper.toDTOWithPermissions(role);
    }

    // Get role by name
    public RoleDTO getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Role not found with name: " + name));

        return roleMapper.toDTO(role);
    }

    // Update role
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO, HttpServletRequest request) {
        // Find existing role
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

        // Check if new name already exists (and it's not the current role)
        if (!existingRole.getName().equals(roleDTO.getName()) &&
                roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() + "' already exists");
        }

        // Update entity from DTO
        roleMapper.updateEntityFromDTO(roleDTO, existingRole);

        // Save updated entity
        Role updatedRole = roleRepository.save(existingRole);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.ROLE_UPDATE, request);

        // Convert to DTO and return
        return roleMapper.toDTO(updatedRole);
    }

    // Delete role
    @Transactional
    public void deleteRole(Long id, HttpServletRequest request) {
        // Check if role exists
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));

        // Check if role has users (prevent deletion if users exist)
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role. " + role.getUsers().size() + " user(s) are assigned to this role");
        }

        // Delete role
        roleRepository.delete(role);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.ROLE_DELETE, request);
    }

    // ===== Permission Management Methods =====

    // Assign permissions to a role
    @Transactional
    public RoleDTO assignPermissionsToRole(RolePermissionDTO rolePermissionDTO, HttpServletRequest request) {
        // Find role
        Role role = roleRepository.findById(rolePermissionDTO.getRoleId())
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + rolePermissionDTO.getRoleId()));

        // Find all permissions by IDs
        Set<Permission> permissions = new HashSet<>();
        for (Long permissionId : rolePermissionDTO.getPermissionIds()) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + permissionId));
            permissions.add(permission);
        }

        // Set permissions to role (replaces existing permissions)
        role.setPermissions(permissions);

        // Save role
        Role savedRole = roleRepository.save(role);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.ROLE_ASSIGN_PERMISSION, request);

        // Return role with permissions
        return roleMapper.toDTOWithPermissions(savedRole);
    }

    // Add single permission to role
    @Transactional
    public RoleDTO addPermissionToRole(Long roleId, Long permissionId, HttpServletRequest request) {
        // Find role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + roleId));

        // Find permission
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + permissionId));

        // Add permission to role
        role.addPermission(permission);

        // Save role
        Role savedRole = roleRepository.save(role);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.ROLE_ASSIGN_PERMISSION, request);

        // Return role with permissions
        return roleMapper.toDTOWithPermissions(savedRole);
    }

    // Remove permission from role
    @Transactional
    public RoleDTO removePermissionFromRole(Long roleId, Long permissionId, HttpServletRequest request) {
        // Find role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + roleId));

        // Find permission
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + permissionId));

        // Remove permission from role
        role.removePermission(permission);

        // Save role
        Role savedRole = roleRepository.save(role);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.ROLE_REMOVE_PERMISSION, request);

        // Return role with permissions
        return roleMapper.toDTOWithPermissions(savedRole);
    }
}
