package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.RoleDTO;
import com.example.eventmanagement.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    @Autowired
    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    // Convert Role Entity to RoleDTO (without permissions)
    public RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());

        return dto;
    }

    // Convert Role Entity to RoleDTO (with permissions)
    public RoleDTO toDTOWithPermissions(Role role) {
        if (role == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());

        // Convert permissions to DTOs
        dto.setPermissions(permissionMapper.toDTOSet(role.getPermissions()));

        return dto;
    }

    // Convert RoleDTO to Role Entity
    public Role toEntity(RoleDTO dto) {
        if (dto == null) {
            return null;
        }

        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());

        return role;
    }

    // Convert Set of Role Entities to Set of RoleDTOs (without permissions)
    public Set<RoleDTO> toDTOSet(Set<Role> roles) {
        if (roles == null) {
            return new HashSet<>();
        }

        return roles.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    // Update existing Role entity with data from RoleDTO
    public void updateEntityFromDTO(RoleDTO dto, Role role) {
        if (dto == null || role == null) {
            return;
        }

        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
    }
}
