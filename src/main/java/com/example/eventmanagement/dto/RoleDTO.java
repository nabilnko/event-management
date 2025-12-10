package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class RoleDTO {

    @Schema(description = "Unique identifier of the role", example = "1")
    private Long id;

    @Schema(description = "Name of the role", example = "ADMIN")
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    private String name;

    @Schema(description = "Description of the role", example = "Can manage events and attendees")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(description = "Permissions assigned to this role")
    private Set<PermissionDTO> permissions;

    // Constructors
    public RoleDTO() {
    }

    public RoleDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public RoleDTO(Long id, String name, String description, Set<PermissionDTO> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionDTO> permissions) {
        this.permissions = permissions;
    }
}
