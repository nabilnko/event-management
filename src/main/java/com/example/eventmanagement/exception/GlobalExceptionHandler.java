package com.example.eventmanagement.exception;

import com.example.eventmanagement.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global Exception Handler for the Event Management System
 * Catches all exceptions thrown by controllers and services
 * Returns consistent error responses with appropriate HTTP status codes
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException (400 BAD REQUEST)
     * Used when: Invalid input, duplicate username/email/role/permission, password validation failures
     *
     * Example scenarios:
     * - Username already exists
     * - Email already registered
     * - Role name already exists
     * - Permission already exists
     * - Passwords don't match
     * - Invalid password (current password incorrect)
     * - New password same as old password
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalStateException (400 BAD REQUEST)
     * Used when: Operation cannot be performed in current state
     *
     * Example scenarios:
     * - Cannot delete role with assigned users
     * - User account is deactivated (cannot login)
     * - Cannot perform operation on inactive resource
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle NoSuchElementException (404 NOT FOUND)
     * Used when: Resource not found (User, Role, Permission, Event)
     *
     * Example scenarios:
     * - User not found with id
     * - Role not found with id
     * - Permission not found with id
     * - Event not found with id
     * - User not found with username
     * - Role not found with name
     * - Permission not found with name
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDto> handleNoSuchElementException(
            NoSuchElementException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle NullPointerException (400 BAD REQUEST)
     * Used when: Required field is null or missing
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponseDto> handleNullPointerException(
            NullPointerException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                "Required field is missing or null",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle BadCredentialsException (401 UNAUTHORIZED)
     * Used when: Login credentials are incorrect
     * Automatically thrown by Spring Security during authentication
     *
     * Example scenarios:
     * - Invalid username
     * - Invalid password
     * - Authentication failure
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle SecurityException (401 UNAUTHORIZED)
     * Used when: Authentication or token validation fails
     *
     * Example scenarios:
     * - Invalid JWT token
     * - Expired JWT token
     * - Token signature verification failed
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDto> handleSecurityException(
            SecurityException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle AccessDeniedException (403 FORBIDDEN)
     * Used when: User doesn't have required permissions/roles
     * Automatically thrown by Spring Security @PreAuthorize annotations
     *
     * Example scenarios:
     * - USER trying to access ADMIN endpoint
     * - ADMIN trying to access SUPER_ADMIN endpoint
     * - Missing required permission
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.FORBIDDEN,
                "You don't have permission to access this resource",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle MethodArgumentNotValidException (400 BAD REQUEST)
     * Used when: @Valid annotation validation fails on request body
     * Returns field-level validation errors
     *
     * Example scenarios:
     * - @NotNull validation failed
     * - @NotBlank validation failed
     * - @Email validation failed
     * - @Size validation failed
     * - @Min/@Max validation failed
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle RuntimeException (500 INTERNAL SERVER ERROR)
     * Used for: Generic runtime errors not covered by specific handlers
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(
            RuntimeException ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle All Other Exceptions (500 INTERNAL SERVER ERROR)
     * Catch-all handler for unexpected errors
     * This should rarely be triggered if all exceptions are properly handled above
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(
            Exception ex,
            WebRequest webRequest) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
