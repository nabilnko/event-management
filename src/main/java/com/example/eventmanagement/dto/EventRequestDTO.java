package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class EventRequestDTO {

    @Schema(description = "Title of the event", example = "Spring Boot Workshop")
    @NotBlank(message = "Event title is required")
    private String title;

    @Schema(description = "Detailed description of the event", example = "Learn Spring Boot from scratch")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Date of the event", example = "2025-12-20", type = "string", format = "date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @Schema(description = "Event start time", example = "10:00:00", type = "string", format = "time")
    @JsonFormat(pattern = "HH:mm:ss")
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @Schema(description = "Event end time", example = "14:00:00", type = "string", format = "time")
    @JsonFormat(pattern = "HH:mm:ss")
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Schema(description = "Location of the event", example = "Dhaka, Bangladesh")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Event type: PUBLIC or PRIVATE", example = "PUBLIC", allowableValues = {"PUBLIC", "PRIVATE"})
    @NotNull(message = "Event type is required")
    private EventType eventType;

    @Schema(description = "List of invited user IDs (required only for PRIVATE events)", example = "[1, 2, 3]")
    private Set<Long> invitedUserIds;

    // Constructors
    public EventRequestDTO() {
    }

    public EventRequestDTO(String title, String description, LocalDate eventDate,
                           LocalTime startTime, LocalTime endTime, String location) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public EventRequestDTO(String title, String description, LocalDate eventDate,
                           LocalTime startTime, LocalTime endTime, String location,
                           EventType eventType, Set<Long> invitedUserIds) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.eventType = eventType;
        this.invitedUserIds = invitedUserIds;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Set<Long> getInvitedUserIds() {
        return invitedUserIds;
    }

    public void setInvitedUserIds(Set<Long> invitedUserIds) {
        this.invitedUserIds = invitedUserIds;
    }
}
