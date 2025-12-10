package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.PermissionDTO;
import com.example.eventmanagement.model.Permission;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    // Convert Permission Entity to PermissionDTO
    public PermissionDTO toDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setPermission(permission.getPermission());
        dto.setDescription(permission.getDescription());

        return dto;
    }

    // Convert PermissionDTO to Permission Entity
    public Permission toEntity(PermissionDTO dto) {
        if (dto == null) {
            return null;
        }

        Permission permission = new Permission();
        permission.setId(dto.getId());
        permission.setPermission(dto.getPermission());
        permission.setDescription(dto.getDescription());

        return permission;
    }

    // Convert List of Permission Entities to List of PermissionDTOs
    public List<PermissionDTO> toDTOList(List<Permission> permissions) {
        if (permissions == null) {
            return null;
        }

        return permissions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Convert Set of Permission Entities to Set of PermissionDTOs
    public Set<PermissionDTO> toDTOSet(Set<Permission> permissions) {
        if (permissions == null) {
            return new HashSet<>();
        }

        return permissions.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    // Update existing Permission entity with data from PermissionDTO
    public void updateEntityFromDTO(PermissionDTO dto, Permission permission) {
        if (dto == null || permission == null) {
            return;
        }

        permission.setPermission(dto.getPermission());
        permission.setDescription(dto.getDescription());
    }
}
