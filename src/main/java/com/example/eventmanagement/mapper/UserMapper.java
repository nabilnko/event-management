package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.UserRequestDTO;
import com.example.eventmanagement.dto.UserResponseDTO;
import com.example.eventmanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // Convert User Entity to UserResponseDTO
    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAge(user.getAge());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Convert role to DTO
        dto.setRole(roleMapper.toDTO(user.getRole()));

        return dto;
    }

    // Convert UserRequestDTO to User Entity
    public User toEntity(UserRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Plain text for now, will encrypt later
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDateOfBirth(dto.getDateOfBirth());
        // Note: Role will be set separately in service layer

        return user;
    }

    // Convert List of User Entities to List of UserResponseDTOs
    public List<UserResponseDTO> toResponseDTOList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Update existing User entity with data from UserRequestDTO
    // Update existing User entity with data from UserRequestDTO
    public void updateEntityFromDTO(UserRequestDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        // Only update password if provided (not empty)
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));  // ENCRYPT PASSWORD
        }

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDateOfBirth(dto.getDateOfBirth());
        // Note: Role will be updated separately in service layer
    }

}
