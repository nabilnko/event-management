package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class EventResponseDTO {

    @Schema(description = "Unique identifier of the event", example = "1")
    private Long id;

    @Schema(description = "Title of the event", example = "Spring Boot Workshop")
    private String title;

    @Schema(description = "Detailed description of the event", example = "Learn Spring Boot from scratch")
    private String description;

    @Schema(description = "Date of the event", example = "2025-12-20", type = "string", format = "date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @Schema(description = "Event start time", example = "10:00:00", type = "string", format = "time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Schema(description = "Event end time", example = "14:00:00", type = "string", format = "time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Schema(description = "Location of the event", example = "Dhaka, Bangladesh")
    private String location;

    @Schema(description = "Event type: PUBLIC or PRIVATE", example = "PUBLIC")
    private EventType eventType;

    @Schema(description = "Event organizer information")
    private UserBasicDTO organizer;

    @Schema(description = "Number of invited users (for PRIVATE events)", example = "5")
    private Integer invitedUsersCount;

    @Schema(description = "List of invited users (only visible to organizer for PRIVATE events)")
    private Set<UserBasicDTO> invitedUsers;

    @Schema(description = "Timestamp when the event was created", example = "2025-11-28T13:03:00.123456")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the event was last updated", example = "2025-11-28T13:03:00.123456")
    private LocalDateTime updatedAt;

    // Constructors
    public EventResponseDTO() {
    }

    public EventResponseDTO(Long id, String title, String description,
                            LocalDate eventDate, LocalTime startTime, LocalTime endTime,
                            String location, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
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

    public UserBasicDTO getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserBasicDTO organizer) {
        this.organizer = organizer;
    }

    public Integer getInvitedUsersCount() {
        return invitedUsersCount;
    }

    public void setInvitedUsersCount(Integer invitedUsersCount) {
        this.invitedUsersCount = invitedUsersCount;
    }

    public Set<UserBasicDTO> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(Set<UserBasicDTO> invitedUsers) {
        this.invitedUsers = invitedUsers;
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
}
