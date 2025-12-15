package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.EventInvitationDTO;
import com.example.eventmanagement.dto.EventRequestDTO;
import com.example.eventmanagement.dto.EventResponseDTO;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.enums.EventType;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import com.example.eventmanagement.util.ApplicationLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final ActivityHistoryService activityHistoryService;
    private final ApplicationLogger applicationLogger;
    private final Logger logger;

    @Autowired
    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventMapper eventMapper,
                        ActivityHistoryService activityHistoryService,
                        ApplicationLogger applicationLogger) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
        this.activityHistoryService = activityHistoryService;
        this.applicationLogger = applicationLogger;
        this.logger = applicationLogger.getLogger(EventService.class);
    }

    /**
     * Create new event with comprehensive validation
     * Automatically sets the current user as organizer
     * Validates: duplicate titles, dates, times, location, event type, invitations
     */
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO requestDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "CREATE", "Event", "New Event");
            logger.info("Creating new event with title: {}", requestDTO.getTitle());

            // Get current authenticated user (event organizer)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User organizer = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            logger.debug("Event organizer: {} (ID: {})", currentUsername, organizer.getId());

            // Validation 1: Check for duplicate event title
            if (eventRepository.existsByTitle(requestDTO.getTitle())) {
                logger.warn("Event creation failed: Duplicate title '{}'", requestDTO.getTitle());
                throw new IllegalArgumentException("Event with title '" + requestDTO.getTitle() + "' already exists");
            }

            // Validation 2: Event location is required
            if (requestDTO.getLocation() == null || requestDTO.getLocation().trim().isEmpty()) {
                logger.warn("Event creation failed: Location is required");
                throw new IllegalArgumentException("Event location is required");
            }

            // Validation 3: Event date cannot be in the past
            if (requestDTO.getEventDate().isBefore(LocalDate.now())) {
                logger.warn("Event creation failed: Event date {} is in the past", requestDTO.getEventDate());
                throw new IllegalArgumentException("Event date cannot be in the past");
            }

            // Validation 4: If event is today, start time cannot be in the past
            if (requestDTO.getEventDate().isEqual(LocalDate.now()) &&
                    requestDTO.getStartTime().isBefore(LocalTime.now())) {
                logger.warn("Event creation failed: Start time {} is in the past for today's event", requestDTO.getStartTime());
                throw new IllegalArgumentException("Event start time cannot be in the past for today's event");
            }

            // Validation 5: End time must be after start time
            if (requestDTO.getEndTime().isBefore(requestDTO.getStartTime())) {
                logger.warn("Event creation failed: End time {} is before start time {}",
                        requestDTO.getEndTime(), requestDTO.getStartTime());
                throw new IllegalArgumentException("Event end time must be after start time");
            }

            // Validation 6: End time cannot be equal to start time
            if (requestDTO.getEndTime().equals(requestDTO.getStartTime())) {
                logger.warn("Event creation failed: End time equals start time");
                throw new IllegalArgumentException("Event end time must be different from start time");
            }

            // Validation 7: Event duration should be reasonable (at least 30 minutes)
            long durationMinutes = java.time.Duration.between(
                    requestDTO.getStartTime(),
                    requestDTO.getEndTime()
            ).toMinutes();

            if (durationMinutes < 30) {
                logger.warn("Event creation failed: Duration {} minutes is less than 30", durationMinutes);
                throw new IllegalArgumentException("Event duration must be at least 30 minutes");
            }

            // Validation 8: Event duration should not exceed 24 hours
            if (durationMinutes > 1440) {
                logger.warn("Event creation failed: Duration {} minutes exceeds 24 hours", durationMinutes);
                throw new IllegalArgumentException("Event duration cannot exceed 24 hours (single day event)");
            }

            // Validation 9: For PRIVATE events, invited users list is required
            if (requestDTO.getEventType() == EventType.PRIVATE) {
                if (requestDTO.getInvitedUserIds() == null || requestDTO.getInvitedUserIds().isEmpty()) {
                    logger.warn("Event creation failed: PRIVATE event must have invited users");
                    throw new IllegalArgumentException("PRIVATE events must have at least one invited user");
                }
            }

            // Create event entity
            Event event = eventMapper.toEntity(requestDTO);
            event.setOrganizer(organizer);

            // Handle invited users for PRIVATE events
            if (requestDTO.getEventType() == EventType.PRIVATE && requestDTO.getInvitedUserIds() != null) {
                Set<User> invitedUsers = new HashSet<>();
                for (Long userId : requestDTO.getInvitedUserIds()) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

                    // Validation: Cannot invite yourself as organizer
                    if (user.getId().equals(organizer.getId())) {
                        logger.warn("Event creation failed: Cannot invite organizer to own event");
                        throw new IllegalArgumentException("Cannot invite yourself as organizer to your own event");
                    }

                    invitedUsers.add(user);
                }
                event.setInvitedUsers(invitedUsers);
                logger.debug("Added {} invited users to PRIVATE event", invitedUsers.size());
            }

            // Save event to database
            Event savedEvent = eventRepository.save(event);
            logger.info("Event created successfully with ID: {}", savedEvent.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.EVENT_CREATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "CREATE", "Event", savedEvent.getId());

            // Return response with invited users if organizer
            return eventMapper.toResponseDTOWithInvitedUsers(savedEvent);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Event creation failed: Entity not found", e);
            throw e;
        } catch (IllegalArgumentException e) {
            applicationLogger.logError(logger, "Event creation failed: Validation error", e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Event creation failed: Unexpected error", e);
            throw new RuntimeException("Failed to create event: " + e.getMessage(), e);
        }
    }

    /**
     * Get all events with pagination
     * Returns only PUBLIC events + events user is invited to + events user organized
     */
    public List<EventResponseDTO> getAllEvents(int page, int size) {
        try {
            logger.debug("Fetching all events - page: {}, size: {}", page, size);

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            // Get accessible events for this user
            List<Event> accessibleEvents = eventRepository.findAccessibleEventsByUserId(currentUser.getId());

            // Apply pagination manually
            int start = page * size;
            int end = Math.min(start + size, accessibleEvents.size());

            if (start > accessibleEvents.size()) {
                logger.debug("No events found for page: {}", page);
                return List.of();
            }

            List<Event> paginatedEvents = accessibleEvents.subList(start, end);
            logger.debug("Found {} events for user: {}", paginatedEvents.size(), currentUsername);

            return eventMapper.toResponseDTOList(paginatedEvents);

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch all events", e);
            throw new RuntimeException("Failed to fetch events: " + e.getMessage(), e);
        }
    }

    /**
     * Get event by ID
     * Checks if user has permission to view the event
     * Throws SecurityException if user cannot access PRIVATE event
     */
    public EventResponseDTO getEventById(Long id) {
        try {
            logger.debug("Fetching event with ID: {}", id);

            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            // Check if user can access this event
            if (!event.canUserAccess(currentUser)) {
                logger.warn("User {} attempted to access private event ID: {}", currentUsername, id);
                throw new SecurityException("You don't have permission to view this private event");
            }

            logger.debug("Event ID {} accessed by user: {}", id, currentUsername);

            // If user is organizer, show full details including invited users
            if (event.isOrganizer(currentUser)) {
                return eventMapper.toResponseDTOWithInvitedUsers(event);
            }

            // Otherwise, show basic details
            return eventMapper.toResponseDTO(event);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Event not found with ID: " + id, e);
            throw e;
        } catch (SecurityException e) {
            applicationLogger.logError(logger, "Access denied to event ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch event ID: " + id, e);
            throw new RuntimeException("Failed to fetch event: " + e.getMessage(), e);
        }
    }

    /**
     * Update event with validation
     * Only organizer can update the event
     * Validates: event exists, not past event, dates, times, location, invitations
     */
    @Transactional
    public EventResponseDTO updateEvent(Long id, EventRequestDTO requestDTO, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "UPDATE", "Event", id);
            logger.info("Updating event with ID: {}", id);

            // Find existing event
            Event existingEvent = eventRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));

            logger.debug("Found event: {} (Organizer: {})", existingEvent.getTitle(), existingEvent.getOrganizer().getUsername());

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            // Validation 0: Only organizer can update the event
            if (!existingEvent.isOrganizer(currentUser)) {
                logger.warn("Event update failed: User {} is not the organizer of event ID: {}", currentUsername, id);
                throw new SecurityException("Only the event organizer can update this event");
            }

            // Validation 1: Cannot update event that has already ended
            LocalDateTime existingEventEnd = LocalDateTime.of(existingEvent.getEventDate(), existingEvent.getEndTime());
            if (existingEventEnd.isBefore(LocalDateTime.now())) {
                logger.warn("Event update failed: Event ID {} has already ended", id);
                throw new IllegalStateException("Cannot update event that has already ended");
            }

            // Validation 2: Check for duplicate title (if title is being changed)
            if (!existingEvent.getTitle().equals(requestDTO.getTitle()) &&
                    eventRepository.existsByTitle(requestDTO.getTitle())) {
                logger.warn("Event update failed: Duplicate title '{}'", requestDTO.getTitle());
                throw new IllegalArgumentException("Event with title '" + requestDTO.getTitle() + "' already exists");
            }

            // Validation 3: Event location is required
            if (requestDTO.getLocation() == null || requestDTO.getLocation().trim().isEmpty()) {
                logger.warn("Event update failed: Location is required");
                throw new IllegalArgumentException("Event location is required");
            }

            // Validation 4: Event date validation
            if (requestDTO.getEventDate().isBefore(LocalDate.now())) {
                logger.warn("Event update failed: Cannot change event date to the past");
                throw new IllegalArgumentException("Cannot change event date to the past");
            }

            // Validation 5: If changing to today, start time cannot be in the past
            if (requestDTO.getEventDate().isEqual(LocalDate.now()) &&
                    requestDTO.getStartTime().isBefore(LocalTime.now())) {
                logger.warn("Event update failed: Start time is in the past for today's event");
                throw new IllegalArgumentException("Event start time cannot be in the past for today's event");
            }

            // Validation 6: End time must be after start time
            if (requestDTO.getEndTime().isBefore(requestDTO.getStartTime())) {
                logger.warn("Event update failed: End time is before start time");
                throw new IllegalArgumentException("Event end time must be after start time");
            }

            // Validation 7: End time cannot be equal to start time
            if (requestDTO.getEndTime().equals(requestDTO.getStartTime())) {
                logger.warn("Event update failed: End time equals start time");
                throw new IllegalArgumentException("Event end time must be different from start time");
            }

            // Validation 8: Event duration validation
            long durationMinutes = java.time.Duration.between(
                    requestDTO.getStartTime(),
                    requestDTO.getEndTime()
            ).toMinutes();

            if (durationMinutes < 30) {
                logger.warn("Event update failed: Duration is less than 30 minutes");
                throw new IllegalArgumentException("Event duration must be at least 30 minutes");
            }

            if (durationMinutes > 1440) {
                logger.warn("Event update failed: Duration exceeds 24 hours");
                throw new IllegalArgumentException("Event duration cannot exceed 24 hours (single day event)");
            }

            // Validation 9: For PRIVATE events, invited users list is required
            if (requestDTO.getEventType() == EventType.PRIVATE) {
                if (requestDTO.getInvitedUserIds() == null || requestDTO.getInvitedUserIds().isEmpty()) {
                    logger.warn("Event update failed: PRIVATE event must have invited users");
                    throw new IllegalArgumentException("PRIVATE events must have at least one invited user");
                }
            }

            // Update entity from DTO
            eventMapper.updateEntityFromDTO(requestDTO, existingEvent);

            // Handle invited users update for PRIVATE events
            if (requestDTO.getEventType() == EventType.PRIVATE && requestDTO.getInvitedUserIds() != null) {
                Set<User> invitedUsers = new HashSet<>();
                for (Long userId : requestDTO.getInvitedUserIds()) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

                    if (user.getId().equals(currentUser.getId())) {
                        logger.warn("Event update failed: Cannot invite organizer to own event");
                        throw new IllegalArgumentException("Cannot invite yourself as organizer to your own event");
                    }

                    invitedUsers.add(user);
                }
                existingEvent.setInvitedUsers(invitedUsers);
                logger.debug("Updated {} invited users for PRIVATE event", invitedUsers.size());
            } else if (requestDTO.getEventType() == EventType.PUBLIC) {
                // Clear invited users if changing from PRIVATE to PUBLIC
                existingEvent.getInvitedUsers().clear();
                logger.debug("Cleared invited users (changed to PUBLIC event)");
            }

            // Save updated event
            Event updatedEvent = eventRepository.save(existingEvent);
            logger.info("Event updated successfully with ID: {}", updatedEvent.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.EVENT_UPDATE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "UPDATE", "Event", updatedEvent.getId());

            return eventMapper.toResponseDTOWithInvitedUsers(updatedEvent);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Event update failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (SecurityException e) {
            applicationLogger.logError(logger, "Event update failed: Security violation for ID: " + id, e);
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            applicationLogger.logError(logger, "Event update failed: Validation error for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Event update failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to update event: " + e.getMessage(), e);
        }
    }

    /**
     * Delete event with validation
     * Only organizer can delete the event
     * Cannot delete past or ongoing events
     */
    @Transactional
    public void deleteEvent(Long id, HttpServletRequest request) {
        try {
            // TRACE LOG: Start of operation
            applicationLogger.logTrace(logger, "DELETE", "Event", id);
            logger.info("Deleting event with ID: {}", id);

            // Find existing event
            Event existingEvent = eventRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));

            logger.debug("Found event to delete: {} (Organizer: {})",
                    existingEvent.getTitle(), existingEvent.getOrganizer().getUsername());

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            // Validation 0: Only organizer can delete the event
            if (!existingEvent.isOrganizer(currentUser)) {
                logger.warn("Event deletion failed: User {} is not the organizer of event ID: {}", currentUsername, id);
                throw new SecurityException("Only the event organizer can delete this event");
            }

            // Validation 1: Cannot delete event that has already started
            LocalDateTime eventStart = LocalDateTime.of(existingEvent.getEventDate(), existingEvent.getStartTime());
            LocalDateTime eventEnd = LocalDateTime.of(existingEvent.getEventDate(), existingEvent.getEndTime());
            LocalDateTime now = LocalDateTime.now();

            if (eventEnd.isBefore(now)) {
                logger.warn("Event deletion failed: Event ID {} has already ended", id);
                throw new IllegalStateException("Cannot delete event that has already ended");
            }

            if (eventStart.isBefore(now) && eventEnd.isAfter(now)) {
                logger.warn("Event deletion failed: Event ID {} is currently ongoing", id);
                throw new IllegalStateException("Cannot delete an ongoing event");
            }

            // Delete event
            eventRepository.delete(existingEvent);
            logger.info("Event deleted successfully with ID: {}", id);

            // Record activity
            activityHistoryService.recordActivity(ActivityType.EVENT_DELETE, request);

            // TRACE LOG: End of operation
            applicationLogger.logTrace(logger, "DELETE", "Event", id);

        } catch (NoSuchElementException e) {
            applicationLogger.logError(logger, "Event deletion failed: Entity not found for ID: " + id, e);
            throw e;
        } catch (SecurityException e) {
            applicationLogger.logError(logger, "Event deletion failed: Security violation for ID: " + id, e);
            throw e;
        } catch (IllegalStateException e) {
            applicationLogger.logError(logger, "Event deletion failed: State validation error for ID: " + id, e);
            throw e;
        } catch (Exception e) {
            applicationLogger.logError(logger, "Event deletion failed: Unexpected error for ID: " + id, e);
            throw new RuntimeException("Failed to delete event: " + e.getMessage(), e);
        }
    }

    // ========== INVITATION MANAGEMENT METHODS ==========

    /**
     * Invite users to a PRIVATE event
     * Only organizer can invite users
     */
    @Transactional
    public EventResponseDTO inviteUsersToEvent(EventInvitationDTO invitationDTO, HttpServletRequest request) {
        try {
            logger.info("Inviting users to event ID: {}", invitationDTO.getEventId());

            // Find event
            Event event = eventRepository.findById(invitationDTO.getEventId())
                    .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + invitationDTO.getEventId()));

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            // Validation 1: Only organizer can invite users
            if (!event.isOrganizer(currentUser)) {
                logger.warn("Invite failed: User {} is not the organizer", currentUsername);
                throw new SecurityException("Only the event organizer can invite users");
            }

            // Validation 2: Can only invite to PRIVATE events
            if (event.getEventType() != EventType.PRIVATE) {
                logger.warn("Invite failed: Event is not PRIVATE");
                throw new IllegalStateException("Can only invite users to PRIVATE events");
            }

            // Add invited users
            for (Long userId : invitationDTO.getUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

                if (user.getId().equals(currentUser.getId())) {
                    logger.warn("Invite failed: Cannot invite organizer");
                    throw new IllegalArgumentException("Cannot invite yourself as organizer");
                }

                event.addInvitedUser(user);
            }

            // Save event
            Event savedEvent = eventRepository.save(event);
            logger.info("Successfully invited {} users to event ID: {}", invitationDTO.getUserIds().size(), event.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.EVENT_UPDATE, request);

            return eventMapper.toResponseDTOWithInvitedUsers(savedEvent);

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to invite users to event", e);
            throw e;
        }
    }

    /**
     * Remove users from a PRIVATE event
     * Only organizer can remove invited users
     */
    @Transactional
    public EventResponseDTO removeUsersFromEvent(EventInvitationDTO invitationDTO, HttpServletRequest request) {
        try {
            logger.info("Removing users from event ID: {}", invitationDTO.getEventId());

            // Find event
            Event event = eventRepository.findById(invitationDTO.getEventId())
                    .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + invitationDTO.getEventId()));

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            // Validation 1: Only organizer can remove users
            if (!event.isOrganizer(currentUser)) {
                logger.warn("Remove failed: User {} is not the organizer", currentUsername);
                throw new SecurityException("Only the event organizer can remove invited users");
            }

            // Validation 2: Can only remove from PRIVATE events
            if (event.getEventType() != EventType.PRIVATE) {
                logger.warn("Remove failed: Event is not PRIVATE");
                throw new IllegalStateException("Can only remove users from PRIVATE events");
            }

            // Remove invited users
            for (Long userId : invitationDTO.getUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

                event.removeInvitedUser(user);
            }

            // Save event
            Event savedEvent = eventRepository.save(event);
            logger.info("Successfully removed {} users from event ID: {}", invitationDTO.getUserIds().size(), event.getId());

            // Record activity
            activityHistoryService.recordActivity(ActivityType.EVENT_UPDATE, request);

            return eventMapper.toResponseDTOWithInvitedUsers(savedEvent);

        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to remove users from event", e);
            throw e;
        }
    }

    // ========== HELPER METHODS (Your existing + new) ==========

    /**
     * Get all PUBLIC events
     */
    public List<EventResponseDTO> getAllPublicEvents() {
        try {
            logger.debug("Fetching all public events");
            List<Event> publicEvents = eventRepository.findAllPublicEvents();
            logger.debug("Found {} public events", publicEvents.size());
            return eventMapper.toResponseDTOList(publicEvents);
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch public events", e);
            throw new RuntimeException("Failed to fetch public events: " + e.getMessage(), e);
        }
    }

    /**
     * Get events organized by current user
     */
    public List<EventResponseDTO> getMyOrganizedEvents() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            logger.debug("Fetching organized events for user: {}", currentUsername);
            List<Event> myEvents = eventRepository.findByOrganizerId(currentUser.getId());
            logger.debug("Found {} organized events", myEvents.size());

            // Show full details for own events
            return myEvents.stream()
                    .map(eventMapper::toResponseDTOWithInvitedUsers)
                    .toList();
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch organized events", e);
            throw new RuntimeException("Failed to fetch organized events: " + e.getMessage(), e);
        }
    }

    /**
     * Get events where current user is invited
     */
    public List<EventResponseDTO> getMyInvitedEvents() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            logger.debug("Fetching invited events for user: {}", currentUsername);
            List<Event> invitedEvents = eventRepository.findEventsByInvitedUserId(currentUser.getId());
            logger.debug("Found {} invited events", invitedEvents.size());

            return eventMapper.toResponseDTOList(invitedEvents);
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch invited events", e);
            throw new RuntimeException("Failed to fetch invited events: " + e.getMessage(), e);
        }
    }

    /**
     * Get upcoming events for current user
     */
    public List<EventResponseDTO> getUpcomingEvents() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NoSuchElementException("Current user not found"));

            LocalDate today = LocalDate.now();
            logger.debug("Fetching upcoming events for user: {}", currentUsername);
            List<Event> upcomingEvents = eventRepository.findUpcomingEventsForUser(currentUser.getId(), today);
            logger.debug("Found {} upcoming events", upcomingEvents.size());

            return eventMapper.toResponseDTOList(upcomingEvents);
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch upcoming events", e);
            throw new RuntimeException("Failed to fetch upcoming events: " + e.getMessage(), e);
        }
    }

    /**
     * Get past events
     */
    public List<EventResponseDTO> getPastEvents() {
        try {
            LocalDate today = LocalDate.now();
            logger.debug("Fetching past events");
            List<Event> pastEvents = eventRepository.findByEventDateBefore(today);
            logger.debug("Found {} past events", pastEvents.size());
            return eventMapper.toResponseDTOList(pastEvents);
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch past events", e);
            throw new RuntimeException("Failed to fetch past events: " + e.getMessage(), e);
        }
    }

    /**
     * Get today's events
     */
    public List<EventResponseDTO> getTodaysEvents() {
        try {
            LocalDate today = LocalDate.now();
            logger.debug("Fetching today's events");
            List<Event> todaysEvents = eventRepository.findByEventDate(today);
            logger.debug("Found {} today's events", todaysEvents.size());
            return eventMapper.toResponseDTOList(todaysEvents);
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch today's events", e);
            throw new RuntimeException("Failed to fetch today's events: " + e.getMessage(), e);
        }
    }

    /**
     * Get events by location
     */
    public List<EventResponseDTO> getEventsByLocation(String location) {
        try {
            logger.debug("Fetching events by location: {}", location);
            List<Event> events = eventRepository.findByLocationContainingIgnoreCase(location);
            logger.debug("Found {} events for location: {}", events.size(), location);
            return eventMapper.toResponseDTOList(events);
        } catch (Exception e) {
            applicationLogger.logError(logger, "Failed to fetch events by location", e);
            throw new RuntimeException("Failed to fetch events by location: " + e.getMessage(), e);
        }
    }
}
