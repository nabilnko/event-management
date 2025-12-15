package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.EventRequestDTO;
import com.example.eventmanagement.dto.EventResponseDTO;
import com.example.eventmanagement.dto.UserBasicDTO;
import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    /**
     * Convert EventRequestDTO to Event entity (without organizer and invited users)
     * Organizer and invited users should be set separately in the service layer
     */
    public Event toEntity(EventRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setLocation(dto.getLocation());
        event.setEventType(dto.getEventType());

        return event;
    }

    /**
     * Convert Event entity to EventResponseDTO
     * Includes organizer and invited users information
     */
    public EventResponseDTO toResponseDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setEventType(event.getEventType());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        // Map organizer
        if (event.getOrganizer() != null) {
            dto.setOrganizer(toUserBasicDTO(event.getOrganizer()));
        }

        // Map invited users count (always show count)
        if (event.getInvitedUsers() != null) {
            dto.setInvitedUsersCount(event.getInvitedUsers().size());
        } else {
            dto.setInvitedUsersCount(0);
        }

        return dto;
    }

    /**
     * Convert Event entity to EventResponseDTO with invited users list
     * Use this when the requester is the organizer or has permission to see invited users
     */
    public EventResponseDTO toResponseDTOWithInvitedUsers(Event event) {
        EventResponseDTO dto = toResponseDTO(event);

        if (dto != null && event.getInvitedUsers() != null) {
            Set<UserBasicDTO> invitedUsersDTO = event.getInvitedUsers().stream()
                    .map(this::toUserBasicDTO)
                    .collect(Collectors.toSet());
            dto.setInvitedUsers(invitedUsersDTO);
        }

        return dto;
    }

    /**
     * Convert list of Event entities to list of EventResponseDTOs
     */
    public List<EventResponseDTO> toResponseDTOList(List<Event> events) {
        if (events == null) {
            return null;
        }

        return events.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update Event entity from EventRequestDTO
     * Does NOT update organizer or invited users (handled separately)
     */
    public void updateEntityFromDTO(EventRequestDTO dto, Event event) {
        if (dto == null || event == null) {
            return;
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setLocation(dto.getLocation());
        event.setEventType(dto.getEventType());
    }

    /**
     * Convert User entity to UserBasicDTO
     */
    private UserBasicDTO toUserBasicDTO(User user) {
        if (user == null) {
            return null;
        }

        UserBasicDTO dto = new UserBasicDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        return dto;
    }
}
