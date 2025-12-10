package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @Schema(description = "Username for login", example = "john_superadmin", required = true)
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "User's password", example = "admin123", required = true)
    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
