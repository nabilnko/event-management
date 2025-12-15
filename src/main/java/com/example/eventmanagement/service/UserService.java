package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.UserRequestDTO;
import com.example.eventmanagement.dto.UserResponseDTO;
import com.example.eventmanagement.dto.ChangePasswordDTO;
import com.example.eventmanagement.dto.ResetPasswordDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.UserMapper;
import com.example.eventmanagement.model.Role;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.RoleRepository;
import com.example.eventmanagement.repository.UserRepository;
import com.example.eventmanagement.util.ApplicationLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivityHistoryService activityHistoryService;
    private final PasswordHistoryService passwordHistoryService;
    private final ApplicationLogger applicationLogger;
    private final Logger logger;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       ActivityHistoryService activityHistoryService,
                       PasswordHistoryService passwordHistoryService,
                       ApplicationLogger applicationLogger) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.activityHistoryService = activityHistoryService;
        this.passwordHistoryService = passwordHistoryService;
        this.applicationLogger = applicationLogger;
        this.logger = applicationLogger.getLogger(UserService.class);
    }

    // Create new user
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO requestDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "CREATE", "User", requestDTO.getUsername());
            logger.info("Creating new user with username: {}", requestDTO.getUsername());

            // Check if username already exists
            if (userRepository.existsByUsername(requestDTO.getUsername())) {
                logger.warn("User creation failed: Username '{}' already exists", requestDTO.getUsername());
                throw new IllegalArgumentException("Username '" + requestDTO.getUsername() + "' is already taken");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(requestDTO.getEmail())) {
                logger.warn("User creation failed: Email '{}' already exists", requestDTO.getEmail());
                throw new IllegalArgumentException("Email '" + requestDTO.getEmail() + "' is already registered");
            }

            // Convert DTO to Entity
            User user = userMapper.toEntity(requestDTO);

            // Encrypt password
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);
            logger.debug("Password encrypted for user: {}", requestDTO.getUsername());

            // Find and assign role
            Role role = roleRepository.findById(requestDTO.getRoleId())
                    .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + requestDTO.getRoleId()));

            user.setRole(role);
            logger.debug("Assigned role '{}' to user: {}", role.getName(), requestDTO.getUsername());

            // Save user to database
            User savedUser = userRepository.save(user);
            logger.info("User created successfully with ID: {}", savedUser.getId());

            // Get current authenticated user (who is creating this user)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String createdBy = authentication != null ? authentication.getName() : "SYSTEM";

            // Record password creation in history
            passwordHistoryService.recordPasswordChange(
                    String.valueOf(savedUser.getId()),
                    createdBy,
                    null, // No old password (new user)
                    encryptedPassword
            );

            // Record activity
            activityHistoryService.recordActivity(ActivityType.USER_CREATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "CREATE", "User", savedUser.getId());

            // Convert to ResponseDTO and return
            return userMapper.toResponseDTO(savedUser);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User creation failed: Role not found", e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "User creation failed: Validation error", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "User creation failed: Unexpected error", e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    // Get all users with pagination
    public List<UserResponseDTO> getAllUsers(int page, int size) {
        try {
            logger.debug("Fetching all users - page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);
            logger.debug("Found {} users", userPage.getTotalElements());

            return userMapper.toResponseDTOList(userPage.getContent());

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch all users", e);
            throw new RuntimeException("Failed to fetch users: " + e.getMessage(), e);
        }
    }

    // Get user by ID
    public UserResponseDTO getUserById(Long id) {
        try {
            logger.debug("Fetching user with ID: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

            logger.debug("Found user: {} ({})", user.getUsername(), user.getEmail());
            return userMapper.toResponseDTO(user);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User not found with ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch user ID: " + id, e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    // Get user by username
    public UserResponseDTO getUserByUsername(String username) {
        try {
            logger.debug("Fetching user with username: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("User not found with username: " + username));

            logger.debug("Found user: {} (ID: {})", user.getUsername(), user.getId());
            return userMapper.toResponseDTO(user);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User not found with username: " + username, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch user by username", e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    // Get all active users
    public List<UserResponseDTO> getActiveUsers() {
        try {
            logger.debug("Fetching all active users");
            List<User> users = userRepository.findByActive(true);
            logger.debug("Found {} active users", users.size());
            return userMapper.toResponseDTOList(users);

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch active users", e);
            throw new RuntimeException("Failed to fetch active users: " + e.getMessage(), e);
        }
    }

    // Update user
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "UPDATE", "User", id);
            logger.info("Updating user with ID: {}", id);

            // Find existing user
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

            logger.debug("Found user to update: {} ({})", existingUser.getUsername(), existingUser.getEmail());

            // Check if username is being changed and if it's already taken
            if (!existingUser.getUsername().equals(requestDTO.getUsername()) &&
                    userRepository.existsByUsername(requestDTO.getUsername())) {
                logger.warn("User update failed: Username '{}' already exists", requestDTO.getUsername());
                throw new IllegalArgumentException("Username '" + requestDTO.getUsername() + "' is already taken");
            }

            // Check if email is being changed and if it's already registered
            if (!existingUser.getEmail().equals(requestDTO.getEmail()) &&
                    userRepository.existsByEmail(requestDTO.getEmail())) {
                logger.warn("User update failed: Email '{}' already exists", requestDTO.getEmail());
                throw new IllegalArgumentException("Email '" + requestDTO.getEmail() + "' is already registered");
            }

            // Update basic fields
            userMapper.updateEntityFromDTO(requestDTO, existingUser);

            // Update password if provided and different
            if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
                String oldPassword = existingUser.getPassword();

                // Check if new password is different from old one
                if (!passwordEncoder.matches(requestDTO.getPassword(), oldPassword)) {
                    String newPassword = passwordEncoder.encode(requestDTO.getPassword());
                    existingUser.setPassword(newPassword);
                    logger.debug("Password updated for user ID: {}", id);

                    // Record password change
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    String changedBy = authentication != null ? authentication.getName() : "SYSTEM";
                    passwordHistoryService.recordPasswordChange(
                            String.valueOf(existingUser.getId()),
                            changedBy,
                            oldPassword,
                            newPassword
                    );
                }
            }

            // Update role if provided
            if (requestDTO.getRoleId() != null) {
                Role role = roleRepository.findById(requestDTO.getRoleId())
                        .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + requestDTO.getRoleId()));
                existingUser.setRole(role);
                logger.debug("Role updated to '{}' for user ID: {}", role.getName(), id);
            }

            // Save updated user
            User updatedUser = userRepository.save(existingUser);
            logger.info("User updated successfully with ID: {}", updatedUser.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.USER_UPDATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "UPDATE", "User", updatedUser.getId());

            // Convert to ResponseDTO and return
            return userMapper.toResponseDTO(updatedUser);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User update failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "User update failed: Validation error for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "User update failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    // Deactivate user (soft delete)
    @Transactional
    public UserResponseDTO deactivateUser(Long id, HttpServletRequest request) {
        try {
            logger.info("Deactivating user with ID: {}", id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

            logger.debug("Deactivating user: {}", user.getUsername());
            user.setActive(false);
            User updatedUser = userRepository.save(user);

            // Record activity
            activityHistoryService.recordActivity(ActivityType.USER_DEACTIVATE, request);

            logger.info("User deactivated successfully: {}", user.getUsername());
            return userMapper.toResponseDTO(updatedUser);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User deactivation failed: User not found with ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "User deactivation failed for ID: " + id, e);
            throw new RuntimeException("Failed to deactivate user: " + e.getMessage(), e);
        }
    }

    // Activate user
    @Transactional
    public UserResponseDTO activateUser(Long id, HttpServletRequest request) {
        try {
            logger.info("Activating user with ID: {}", id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

            logger.debug("Activating user: {}", user.getUsername());
            user.setActive(true);
            User updatedUser = userRepository.save(user);

            // Record activity
            activityHistoryService.recordActivity(ActivityType.USER_ACTIVATE, request);

            logger.info("User activated successfully: {}", user.getUsername());
            return userMapper.toResponseDTO(updatedUser);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User activation failed: User not found with ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "User activation failed for ID: " + id, e);
            throw new RuntimeException("Failed to activate user: " + e.getMessage(), e);
        }
    }

    // Delete user (hard delete)
    @Transactional
    public void deleteUser(Long id, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "DELETE", "User", id);
            logger.info("Deleting user with ID: {}", id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

            logger.debug("Found user to delete: {} ({})", user.getUsername(), user.getEmail());

            userRepository.delete(user);
            logger.info("User deleted successfully with ID: {}", id);

            // Record activity
            activityHistoryService.recordActivity(ActivityType.USER_DELETE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "DELETE", "User", id);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "User deletion failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "User deletion failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    // Change own password
    @Transactional
    public void changeMyPassword(ChangePasswordDTO changePasswordDTO, HttpServletRequest request) {
        try {
            logger.info("User attempting to change their own password");

            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Find user by username
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            logger.debug("Password change requested by user: {}", currentUsername);

            // Verify current password
            if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
                logger.warn("Password change failed: Incorrect current password for user: {}", currentUsername);
                throw new IllegalArgumentException("Current password is incorrect");
            }

            // Verify new password and confirm password match
            if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
                logger.warn("Password change failed: New password and confirm password do not match");
                throw new IllegalArgumentException("New password and confirm password do not match");
            }

            // Check if new password is same as old password
            if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
                logger.warn("Password change failed: New password is same as current password");
                throw new IllegalArgumentException("New password must be different from current password");
            }

            // Save old password for history
            String oldPassword = user.getPassword();

            // Encode and set new password
            String newEncodedPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
            user.setPassword(newEncodedPassword);

            // Save user
            userRepository.save(user);
            logger.info("Password changed successfully for user: {}", currentUsername);

            // Record password change in history
            passwordHistoryService.recordPasswordChange(
                    String.valueOf(user.getId()),
                    currentUsername,
                    oldPassword,
                    newEncodedPassword
            );

            // Record activity
            activityHistoryService.recordActivity(
                    ActivityType.PASSWORD_CHANGE,
                    request,
                    "User",
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    String.format("User '%s' changed their own password", user.getUsername())
            );

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Password change failed: User not found", e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Password change failed: Validation error", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Password change failed: Unexpected error", e);
            throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
        }
    }

    /**
     * Allow SUPER_ADMIN to reset any user's password
     * Does not require old password
     */
    @Transactional
    public void resetUserPassword(Long userId, ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        try {
            logger.info("Password reset requested for user ID: {}", userId);

            // Find user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

            logger.debug("Resetting password for user: {}", user.getUsername());

            // Verify new password and confirm password match
            if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
                logger.warn("Password reset failed: New password and confirm password do not match");
                throw new IllegalArgumentException("New password and confirm password do not match");
            }

            // Save old password for history
            String oldPassword = user.getPassword();

            // Encode and set new password
            String newEncodedPassword = passwordEncoder.encode(resetPasswordDTO.getNewPassword());
            user.setPassword(newEncodedPassword);

            // Save user
            userRepository.save(user);

            // Get who is resetting the password
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String resetBy = authentication != null ? authentication.getName() : "SYSTEM";

            logger.info("Password reset successfully for user: {} by: {}", user.getUsername(), resetBy);

            // Record password change in history
            passwordHistoryService.recordPasswordChange(
                    String.valueOf(user.getId()),
                    resetBy,
                    oldPassword,
                    newEncodedPassword
            );

            // Record activity
            activityHistoryService.recordActivity(
                    ActivityType.PASSWORD_RESET,
                    request,
                    "User",
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    String.format("SUPER_ADMIN '%s' reset password for user '%s'", resetBy, user.getUsername())
            );

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Password reset failed: User not found with ID: " + userId, e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Password reset failed: Validation error for user ID: " + userId, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Password reset failed: Unexpected error for user ID: " + userId, e);
            throw new RuntimeException("Failed to reset password: " + e.getMessage(), e);
        }
    }
}
