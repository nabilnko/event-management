package com.example.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "DTO for inviting/removing users to/from private events")
public class EventInvitationDTO {

    @Schema(description = "Event ID", example = "1")
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @Schema(description = "List of user IDs to invite or remove", example = "[1, 2, 3]")
    @NotEmpty(message = "At least one user ID is required")
    private Set<Long> userIds;

    // Constructors
    public EventInvitationDTO() {
    }

    public EventInvitationDTO(Long eventId, Set<Long> userIds) {
        this.eventId = eventId;
        this.userIds = userIds;
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Set<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<Long> userIds) {
        this.userIds = userIds;
    }
}
