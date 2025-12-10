package com.example.eventmanagement.controller;

import com.example.eventmanagement.model.UserActivityHistory;
import com.example.eventmanagement.model.UserLoginLogoutHistory;
import com.example.eventmanagement.model.UserPasswordHistory;
import com.example.eventmanagement.service.ActivityHistoryService;
import com.example.eventmanagement.service.LoginLogoutHistoryService;
import com.example.eventmanagement.service.PasswordHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "6. Audit & History", description = "APIs for viewing audit logs, activity history, and login history")
@RestController
@RequestMapping("/history")
@SecurityRequirement(name = "bearerAuth")
public class HistoryController {

    private final ActivityHistoryService activityHistoryService;
    private final LoginLogoutHistoryService loginLogoutHistoryService;
    private final PasswordHistoryService passwordHistoryService;

    @Autowired
    public HistoryController(ActivityHistoryService activityHistoryService,
                             LoginLogoutHistoryService loginLogoutHistoryService,
                             PasswordHistoryService passwordHistoryService) {
        this.activityHistoryService = activityHistoryService;
        this.loginLogoutHistoryService = loginLogoutHistoryService;
        this.passwordHistoryService = passwordHistoryService;
    }

    // ==================== ACTIVITY HISTORY ====================

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get My Activity History",
            description = "Returns activity history for the currently logged-in user. Everyone can see their own activities."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved activity history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required")
    })
    @GetMapping("/my-activities")
    public ResponseEntity<List<UserActivityHistory>> getMyActivities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<UserActivityHistory> activities = activityHistoryService.getUserActivities(username);
        return ResponseEntity.ok(activities);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Get User Activity History by Username",
            description = "Returns activity history for a specific user. Only SUPER_ADMIN and ADMIN can view other users' activities."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved activity history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN or ADMIN role required")
    })
    @GetMapping("/activities/user/{username}")
    public ResponseEntity<List<UserActivityHistory>> getUserActivities(
            @Parameter(description = "Username to get activities for", required = true, example = "admin")
            @PathVariable String username) {
        List<UserActivityHistory> activities = activityHistoryService.getUserActivities(username);
        return ResponseEntity.ok(activities);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get Activities by Type",
            description = "Returns all activities of a specific type. Only SUPER_ADMIN can view this."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved activities"),
            @ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @GetMapping("/activities/type/{activityTypeCode}")
    public ResponseEntity<List<UserActivityHistory>> getActivitiesByType(
            @Parameter(description = "Activity type code", required = true, example = "USER_CREATE")
            @PathVariable String activityTypeCode) {
        List<UserActivityHistory> activities = activityHistoryService.getActivitiesByType(activityTypeCode);
        return ResponseEntity.ok(activities);
    }

    // ==================== LOGIN HISTORY ====================

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get My Login History",
            description = "Returns login/logout history for the currently logged-in user. Everyone can see their own login history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved login history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @GetMapping("/my-logins")
    public ResponseEntity<List<UserLoginLogoutHistory>> getMyLoginHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get user ID from username - you might need to adjust this
        List<UserLoginLogoutHistory> loginHistory = loginLogoutHistoryService.getUserLoginHistory(username);
        return ResponseEntity.ok(loginHistory);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Get User Login History by User ID",
            description = "Returns login/logout history for a specific user. Only SUPER_ADMIN and ADMIN can view other users' login history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved login history"),
            @ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN or ADMIN role required")
    })
    @GetMapping("/logins/user/{userId}")
    public ResponseEntity<List<UserLoginLogoutHistory>> getUserLoginHistory(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable String userId) {
        List<UserLoginLogoutHistory> loginHistory = loginLogoutHistoryService.getUserLoginHistory(userId);
        return ResponseEntity.ok(loginHistory);
    }

    // ==================== PASSWORD HISTORY ====================

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get My Password Change History",
            description = "Returns password change history for the currently logged-in user. Everyone can see their own password history (without actual passwords)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved password history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @GetMapping("/my-password-changes")
    public ResponseEntity<List<UserPasswordHistory>> getMyPasswordHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get user ID from username
        List<UserPasswordHistory> passwordHistory = passwordHistoryService.getUserPasswordHistory(username);
        return ResponseEntity.ok(passwordHistory);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get User Password Change History by User ID",
            description = "Returns password change history for a specific user. Only SUPER_ADMIN can view other users' password history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved password history"),
            @ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @GetMapping("/password-changes/user/{userId}")
    public ResponseEntity<List<UserPasswordHistory>> getUserPasswordHistory(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable String userId) {
        List<UserPasswordHistory> passwordHistory = passwordHistoryService.getUserPasswordHistory(userId);
        return ResponseEntity.ok(passwordHistory);
    }
}
