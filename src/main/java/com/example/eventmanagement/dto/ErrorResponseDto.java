package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response structure for all API errors"
)
public class ErrorResponseDto {

    @Schema(
            description = "API endpoint path that caused the error",
            example = "uri=/api/users/999"
    )
    private String apiPath;

    @Schema(
            description = "HTTP status code of the error",
            example = "NOT_FOUND"
    )
    private HttpStatus errorCode;

    @Schema(
            description = "Detailed error message explaining what went wrong",
            example = "User not found with id: 999"
    )
    private String errorMessage;

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2025-12-11T15:04:30.123456"
    )
    private LocalDateTime errorTime;

    // Constructors
    public ErrorResponseDto() {
    }

    public ErrorResponseDto(String apiPath, HttpStatus errorCode, String errorMessage, LocalDateTime errorTime) {
        this.apiPath = apiPath;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorTime = errorTime;
    }

    // Getters and Setters
    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public HttpStatus getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(HttpStatus errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(LocalDateTime errorTime) {
        this.errorTime = errorTime;
    }
}
