package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.RoleDTO;
import com.example.eventmanagement.dto.RolePermissionDTO;
import com.example.eventmanagement.service.RoleService;
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

@Tag(name = "Role Management", description = "APIs for managing roles and role-permission assignments. All modification operations are tracked in audit logs.")
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Create a new role",
            description = "Creates a new role (SUPER_ADMIN, ADMIN, MANAGER, ATTENDEE). Role name must be unique. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input or role name already exists", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO,
                                              HttpServletRequest request) {
        RoleDTO createdRole = roleService.createRole(roleDTO, request);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve all roles",
            description = "Fetches all available roles in the system without permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of roles", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve all roles with permissions",
            description = "Fetches all roles including their assigned permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of roles with permissions", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/with-permissions")
    public ResponseEntity<List<RoleDTO>> getAllRolesWithPermissions() {
        List<RoleDTO> roles = roleService.getAllRolesWithPermissions();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a role by ID",
            description = "Fetches a specific role by its unique identifier without permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the role", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(
            @Parameter(description = "ID of the role to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a role by ID with permissions",
            description = "Fetches a specific role including all its assigned permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the role with permissions", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/{id}/with-permissions")
    public ResponseEntity<RoleDTO> getRoleByIdWithPermissions(
            @Parameter(description = "ID of the role to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        RoleDTO role = roleService.getRoleByIdWithPermissions(id);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a role by name",
            description = "Fetches a specific role by its name (SUPER_ADMIN, ADMIN, ATTENDEE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the role", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<RoleDTO> getRoleByName(
            @Parameter(description = "Name of the role", required = true, example = "ADMIN")
            @PathVariable String name) {
        RoleDTO role = roleService.getRoleByName(name);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Update an existing role",
            description = "Updates role name and description. Role name must be unique. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input or role name already exists", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(
            @Parameter(description = "ID of the role to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO roleDTO,
            HttpServletRequest request) {
        RoleDTO updatedRole = roleService.updateRole(id, roleDTO, request);
        return new ResponseEntity<>(updatedRole, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Delete a role",
            description = "Permanently deletes a role. Cannot delete if users are assigned to this role. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role deleted successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Cannot delete role with assigned users", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(
            @Parameter(description = "ID of the role to delete", required = true, example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        roleService.deleteRole(id, request);
        return new ResponseEntity<>("Role deleted successfully", HttpStatus.OK);
    }

    // ===== Permission Management Endpoints =====

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Assign permissions to a role",
            description = "Assigns multiple permissions to a role. Replaces existing permissions. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions assigned successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role or permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PostMapping("/assign-permissions")
    public ResponseEntity<RoleDTO> assignPermissionsToRole(@Valid @RequestBody RolePermissionDTO rolePermissionDTO,
                                                           HttpServletRequest request) {
        RoleDTO role = roleService.assignPermissionsToRole(rolePermissionDTO, request);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Add a single permission to a role",
            description = "Adds one permission to a role without affecting existing permissions. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission added successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role or permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleDTO> addPermissionToRole(
            @Parameter(description = "ID of the role", required = true, example = "2")
            @PathVariable Long roleId,
            @Parameter(description = "ID of the permission to add", required = true, example = "1")
            @PathVariable Long permissionId,
            HttpServletRequest request) {
        RoleDTO role = roleService.addPermissionToRole(roleId, permissionId, request);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Remove a permission from a role",
            description = "Removes one permission from a role. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission removed successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Role or permission not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleDTO> removePermissionFromRole(
            @Parameter(description = "ID of the role", required = true, example = "2")
            @PathVariable Long roleId,
            @Parameter(description = "ID of the permission to remove", required = true, example = "1")
            @PathVariable Long permissionId,
            HttpServletRequest request) {
        RoleDTO role = roleService.removePermissionFromRole(roleId, permissionId, request);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }
}
