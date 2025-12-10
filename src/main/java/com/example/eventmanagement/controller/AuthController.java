package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.LoginRequestDTO;
import com.example.eventmanagement.dto.LoginResponseDTO;
import com.example.eventmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "User Login",
            description = "Authenticate user with username and password. Returns JWT token on success. Login attempts are tracked in history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid request format", content = @Content())
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                                  HttpServletRequest request) {
        LoginResponseDTO response = authService.login(loginRequest, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
