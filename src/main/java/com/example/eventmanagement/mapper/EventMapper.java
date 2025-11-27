package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.EventRequestDTO;
import com.example.eventmanagement.dto.EventResponseDTO;
import com.example.eventmanagement.model.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

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

        return event;
    }

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
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        return dto;
    }

    public List<EventResponseDTO> toResponseDTOList(List<Event> events) {
        if (events == null) {
            return null;
        }

        return events.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

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
    }
}
