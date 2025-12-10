package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.UserRequestDTO;
import com.example.eventmanagement.dto.UserResponseDTO;
import com.example.eventmanagement.service.UserService;
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
import com.example.eventmanagement.dto.ChangePasswordDTO;
import com.example.eventmanagement.dto.ResetPasswordDTO;

import java.util.List;

@Tag(name = "User Management", description = "APIs for managing users - Create, Read, Update, Delete, Activate, Deactivate. All modification operations are tracked in audit logs.")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user with username, email, password, and one role. Role ID is required. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input, username or email already exists, or role not found", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO,
                                                      HttpServletRequest request) {
        UserResponseDTO createdUser = userService.createUser(requestDTO, request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve all users",
            description = "Fetches a paginated list of all users with their assigned role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<UserResponseDTO> users = userService.getAllUsers(page, size);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a user by ID",
            description = "Fetches a specific user by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the user", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve a user by username",
            description = "Fetches a specific user by their username"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the user", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @Parameter(description = "Username of the user", required = true, example = "john_admin")
            @PathVariable String username) {
        UserResponseDTO user = userService.getUserByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Retrieve all active users",
            description = "Fetches all users with active status = true"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active users", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN or ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @GetMapping("/active")
    public ResponseEntity<List<UserResponseDTO>> getActiveUsers() {
        List<UserResponseDTO> users = userService.getActiveUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Update an existing user",
            description = "Updates user information and role. Leave password empty to keep existing password. Role ID is required. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input, username or email already exists, or role not found", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID of the user to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO,
            HttpServletRequest request) {
        UserResponseDTO updatedUser = userService.updateUser(id, requestDTO, request);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Deactivate a user",
            description = "Sets user's active status to false. User cannot log in but data is preserved. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDTO> deactivateUser(
            @Parameter(description = "ID of the user to deactivate", required = true, example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        UserResponseDTO user = userService.deactivateUser(id, request);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Activate a user",
            description = "Sets user's active status to true. User can log in again. This action is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDTO> activateUser(
            @Parameter(description = "ID of the user to activate", required = true, example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        UserResponseDTO user = userService.activateUser(id, request);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Delete a user",
            description = "Permanently deletes a user from the system. This action cannot be undone and is logged in activity history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN role required", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        userService.deleteUser(id, request);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Change My Password",
            description = "Allows any authenticated user (SUPER_ADMIN, ADMIN, ATTENDEE) to change their own password. Requires current password verification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - passwords don't match or current password is incorrect"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/change-my-password")
    public ResponseEntity<String> changeMyPassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
            HttpServletRequest request) {
        userService.changeMyPassword(changePasswordDTO, request);
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Reset User Password (Admin Only)",
            description = "Allows SUPER_ADMIN to reset any user's password without knowing their current password. Use this for password recovery or administrative resets."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - passwords don't match"),
            @ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<String> resetUserPassword(
            @Parameter(description = "ID of the user whose password to reset", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
            HttpServletRequest request) {
        userService.resetUserPassword(id, resetPasswordDTO, request);
        return new ResponseEntity<>("User password reset successfully", HttpStatus.OK);
    }
}
