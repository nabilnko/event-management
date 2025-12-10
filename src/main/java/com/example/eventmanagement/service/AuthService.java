package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.LoginRequestDTO;
import com.example.eventmanagement.dto.LoginResponseDTO;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.UserRepository;
import com.example.eventmanagement.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final LoginLogoutHistoryService loginLogoutHistoryService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       UserRepository userRepository,
                       LoginLogoutHistoryService loginLogoutHistoryService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.loginLogoutHistoryService = loginLogoutHistoryService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest, HttpServletRequest request) {

        // Get user from database first
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails, user.getRole().getName());

            // Record successful login
            loginLogoutHistoryService.recordLogin(
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    user.getRole().getName(),
                    jwt,
                    request,
                    "SUCCESS"
            );

            // Create response
            return new LoginResponseDTO(
                    jwt,
                    user.getUsername(),
                    user.getRole().getName(),
                    jwtUtil.getExpirationTime()
            );

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            loginLogoutHistoryService.recordLogin(
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    user.getRole().getName(),
                    "",
                    request,
                    "FAILED"
            );

            throw new RuntimeException("Invalid username or password");
        }
    }
}
