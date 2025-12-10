package com.example.eventmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Schema(description = "Timestamp when the event was created", example = "2025-11-28T13:03:00.123456")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the event was last updated", example = "2025-11-28T13:03:00.123456")
    private LocalDateTime updatedAt;

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
