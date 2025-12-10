package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.PermissionDTO;
import com.example.eventmanagement.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Permission Management", description = "APIs for managing permissions (resource.action format). All modification operations are tracked in audit logs.")
@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Create a new permission",
            description = "Creates a new permission using resource.action format. Examples: event.create, event.edit, event.delete, user.create, role.assign. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Permission created successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input or permission already exists", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PostMapping
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionDTO permissionDTO,
                                                          HttpServletRequest request) {
        PermissionDTO createdPermission = permissionService.createPermission(permissionDTO, request);
        return new ResponseEntity<>(createdPermission, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve all permissions",
            description = "Fetches all permissions in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of permissions", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a permission by ID",
            description = "Fetches a specific permission by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the permission", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDTO> getPermissionById(
            @Parameter(description = "ID of the permission to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        PermissionDTO permission = permissionService.getPermissionById(id);
        return new ResponseEntity<>(permission, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a permission by name",
            description = "Fetches a specific permission by its name (e.g., event.create, user.edit)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the permission", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/name/{permissionName}")
    public ResponseEntity<PermissionDTO> getPermissionByName(
            @Parameter(description = "Name of the permission", required = true, example = "event.create")
            @PathVariable String permissionName) {
        PermissionDTO permission = permissionService.getPermissionByName(permissionName);
        return new ResponseEntity<>(permission, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Update an existing permission",
            description = "Updates permission name and description. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission updated successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input or permission name already exists", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<PermissionDTO> updatePermission(
            @Parameter(description = "ID of the permission to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PermissionDTO permissionDTO,
            HttpServletRequest request) {
        PermissionDTO updatedPermission = permissionService.updatePermission(id, permissionDTO, request);
        return new ResponseEntity<>(updatedPermission, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Delete a permission",
            description = "Permanently deletes a permission from the system. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission deleted successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermission(
            @Parameter(description = "ID of the permission to delete", required = true, example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        permissionService.deletePermission(id, request);
        return new ResponseEntity<>("Permission deleted successfully", HttpStatus.OK);
    }
}
