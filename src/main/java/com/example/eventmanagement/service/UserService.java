package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.UserRequestDTO;
import com.example.eventmanagement.dto.UserResponseDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.UserMapper;
import com.example.eventmanagement.model.Role;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.RoleRepository;
import com.example.eventmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.eventmanagement.dto.ChangePasswordDTO;
import com.example.eventmanagement.dto.ResetPasswordDTO;
import com.example.eventmanagement.enums.ActivityType;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivityHistoryService activityHistoryService;
    private final PasswordHistoryService passwordHistoryService;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       ActivityHistoryService activityHistoryService,
                       PasswordHistoryService passwordHistoryService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.activityHistoryService = activityHistoryService;
        this.passwordHistoryService = passwordHistoryService;
    }

    // Create new user
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO requestDTO, HttpServletRequest request) {
// Check if username already exists
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new RuntimeException("Username '" + requestDTO.getUsername() + "' is already taken");
        }

// Check if email already exists
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email '" + requestDTO.getEmail() + "' is already registered");
        }

// Convert DTO to Entity
        User user = userMapper.toEntity(requestDTO);

// Encrypt password
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

// Find and assign role
        Role role = roleRepository.findById(requestDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + requestDTO.getRoleId()));

        user.setRole(role);

// Save user to database
        User savedUser = userRepository.save(user);

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

// Convert to ResponseDTO and return
        return userMapper.toResponseDTO(savedUser);
    }

    // Get all users with pagination
    public List<UserResponseDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        return userMapper.toResponseDTOList(userPage.getContent());
    }

    // Get user by ID
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return userMapper.toResponseDTO(user);
    }

    // Get user by username
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        return userMapper.toResponseDTO(user);
    }

    // Get all active users
    public List<UserResponseDTO> getActiveUsers() {
        List<User> users = userRepository.findByActive(true);
        return userMapper.toResponseDTOList(users);
    }

    // Update user
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO, HttpServletRequest request) {
// Find existing user
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

// Check if username is being changed and if it's already taken
        if (!existingUser.getUsername().equals(requestDTO.getUsername()) &&
                userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new RuntimeException("Username '" + requestDTO.getUsername() + "' is already taken");
        }

// Check if email is being changed and if it's already registered
        if (!existingUser.getEmail().equals(requestDTO.getEmail()) &&
                userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email '" + requestDTO.getEmail() + "' is already registered");
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
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + requestDTO.getRoleId()));
            existingUser.setRole(role);
        }

// Save updated user
        User updatedUser = userRepository.save(existingUser);

// Record activity
        activityHistoryService.recordActivity(ActivityType.USER_UPDATE, request);

// Convert to ResponseDTO and return
        return userMapper.toResponseDTO(updatedUser);
    }

    // Deactivate user (soft delete)
    @Transactional
    public UserResponseDTO deactivateUser(Long id, HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setActive(false);
        User updatedUser = userRepository.save(user);

// Record activity
        activityHistoryService.recordActivity(ActivityType.USER_DEACTIVATE, request);

        return userMapper.toResponseDTO(updatedUser);
    }

    // Activate user
    @Transactional
    public UserResponseDTO activateUser(Long id, HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setActive(true);
        User updatedUser = userRepository.save(user);

// Record activity
        activityHistoryService.recordActivity(ActivityType.USER_ACTIVATE, request);

        return userMapper.toResponseDTO(updatedUser);
    }

    // Delete user (hard delete)
    @Transactional
    public void deleteUser(Long id, HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepository.delete(user);

// Record activity
        activityHistoryService.recordActivity(ActivityType.USER_DELETE, request);
    }

    @Transactional
    public void changeMyPassword(ChangePasswordDTO changePasswordDTO, HttpServletRequest request) {
// Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

// Find user by username
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

// Verify current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

// Verify new password and confirm password match
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

// Check if new password is same as old password
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

// Save old password for history
        String oldPassword = user.getPassword();

// Encode and set new password
        String newEncodedPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        user.setPassword(newEncodedPassword);

// Save user
        userRepository.save(user);

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
    }

    /**
     * Allow SUPER_ADMIN to reset any user's password
     * Does not require old password
     */
    @Transactional
    public void resetUserPassword(Long userId, ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
// Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

// Verify new password and confirm password match
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
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
    }
}