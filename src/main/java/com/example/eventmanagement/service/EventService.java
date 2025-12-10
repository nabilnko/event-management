package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.EventRequestDTO;
import com.example.eventmanagement.dto.EventResponseDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ActivityHistoryService activityHistoryService;

    @Autowired
    public EventService(EventRepository eventRepository,
                        EventMapper eventMapper,
                        ActivityHistoryService activityHistoryService) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.activityHistoryService = activityHistoryService;
    }

    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO requestDTO, HttpServletRequest request) {
        Event event = eventMapper.toEntity(requestDTO);

        Event savedEvent = eventRepository.save(event);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.EVENT_CREATE, request);

        return eventMapper.toResponseDTO(savedEvent);
    }

    public List<EventResponseDTO> getAllEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventRepository.findAll(pageable);

        return eventMapper.toResponseDTOList(eventPage.getContent());
    }

    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        return eventMapper.toResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO updateEvent(Long id, EventRequestDTO requestDTO, HttpServletRequest request) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        eventMapper.updateEntityFromDTO(requestDTO, existingEvent);

        Event updatedEvent = eventRepository.save(existingEvent);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.EVENT_UPDATE, request);

        return eventMapper.toResponseDTO(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id, HttpServletRequest request) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        eventRepository.delete(existingEvent);

        // Record activity
        activityHistoryService.recordActivity(ActivityType.EVENT_DELETE, request);
    }
}
