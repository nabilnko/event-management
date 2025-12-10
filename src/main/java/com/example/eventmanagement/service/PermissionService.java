package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.PermissionDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.PermissionMapper;
import com.example.eventmanagement.model.Permission;
import com.example.eventmanagement.repository.PermissionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final ActivityHistoryService activityHistoryService;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository,
                             PermissionMapper permissionMapper,
                             ActivityHistoryService activityHistoryService) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.activityHistoryService = activityHistoryService;
    }

    // Create new permission
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO, HttpServletRequest request) {
        // Check if permission already exists
        if (permissionRepository.existsByPermission(permissionDTO.getPermission())) {
            throw new RuntimeException("Permission '" + permissionDTO.getPermission() + "' already exists");
        }

        // Convert DTO to Entity
        Permission permission = permissionMapper.toEntity(permissionDTO);

        // Save to database
        Permission savedPermission = permissionRepository.save(permission);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.PERMISSION_CREATE, request);

        // Convert back to DTO and return
        return permissionMapper.toDTO(savedPermission);
    }

    // Get all permissions
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissionMapper.toDTOList(permissions);
    }

    // Get permission by ID
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        return permissionMapper.toDTO(permission);
    }

    // Get permission by name
    public PermissionDTO getPermissionByName(String permissionName) {
        Permission permission = permissionRepository.findByPermission(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found with name: " + permissionName));

        return permissionMapper.toDTO(permission);
    }

    // Update permission
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO, HttpServletRequest request) {
        // Find existing permission
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        // Check if new name already exists (and it's not the current permission)
        if (!existingPermission.getPermission().equals(permissionDTO.getPermission()) &&
                permissionRepository.existsByPermission(permissionDTO.getPermission())) {
            throw new RuntimeException("Permission '" + permissionDTO.getPermission() + "' already exists");
        }

        // Update entity from DTO
        permissionMapper.updateEntityFromDTO(permissionDTO, existingPermission);

        // Save updated permission
        Permission updatedPermission = permissionRepository.save(existingPermission);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.PERMISSION_UPDATE, request);

        // Convert to DTO and return
        return permissionMapper.toDTO(updatedPermission);
    }

    // Delete permission
    @Transactional
    public void deletePermission(Long id, HttpServletRequest request) {
        // Check if permission exists
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        // Delete permission
        permissionRepository.delete(permission);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.PERMISSION_DELETE, request);
    }
}
