package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PermissionDTO {

    @Schema(description = "Unique identifier of the permission", example = "1")
    private Long id;

    @Schema(description = "Name of the permission in resource.action format", example = "event.create")
    @NotBlank(message = "Permission name is required")
    @Size(max = 50, message = "Permission name must not exceed 50 characters")
    private String permission;

    @Schema(description = "Description of what this permission allows", example = "Allows user to create new events")
    @NotBlank(message = "Description is required")
    @Size(max = 100, message = "Description must not exceed 100 characters")
    private String description;

    // Constructors
    public PermissionDTO() {
    }

    public PermissionDTO(Long id, String permission, String description) {
        this.id = id;
        this.permission = permission;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
