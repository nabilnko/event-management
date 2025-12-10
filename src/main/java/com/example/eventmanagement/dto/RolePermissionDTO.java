package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public class RolePermissionDTO {

    @Schema(description = "Role ID", example = "2", required = true)
    @NotNull(message = "Role ID is required")
    private Long roleId;

    @Schema(description = "Set of permission IDs to assign to this role", example = "[1, 2, 3]", required = true)
    @NotNull(message = "Permission IDs are required")
    private Set<Long> permissionIds;

    // Constructors
    public RolePermissionDTO() {
    }

    public RolePermissionDTO(Long roleId, Set<Long> permissionIds) {
        this.roleId = roleId;
        this.permissionIds = permissionIds;
    }

    // Getters and Setters
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Set<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
