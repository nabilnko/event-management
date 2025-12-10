package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.LocalDate;


public class UserResponseDTO {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john_admin")
    private String username;

    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's phone number", example = "+8801712345678")
    private String phoneNumber;

    @Schema(description = "User's date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "User's age calculated from date of birth", example = "35")
    private Integer age;


    @Schema(description = "Account active status", example = "true")
    private Boolean active;

    @Schema(description = "Role assigned to the user")
    private RoleDTO role;

    @Schema(description = "Account creation timestamp", example = "2025-12-02T13:55:00.123456")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-12-02T13:55:00.123456")
    private LocalDateTime updatedAt;

    // Constructors
    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String username, String email, String fullName,
                           String phoneNumber, LocalDate dateOfBirth, Integer age,
                           Boolean active, RoleDTO role,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.active = active;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
