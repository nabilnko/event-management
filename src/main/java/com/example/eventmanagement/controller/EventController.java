package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.EventInvitationDTO;
import com.example.eventmanagement.dto.EventRequestDTO;
import com.example.eventmanagement.dto.EventResponseDTO;
import com.example.eventmanagement.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Event Management", description = "APIs for managing PUBLIC and PRIVATE events. PRIVATE events require invitations. All operations are tracked in audit logs.")
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ========== EXISTING ENDPOINTS (Your original code - updated) ==========

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Create a new event (PUBLIC or PRIVATE)",
            description = "Creates a new event. Current user automatically becomes the organizer. For PRIVATE events, provide invitedUserIds. For PUBLIC events, anyone can view."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied - Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO requestDTO,
                                                        HttpServletRequest request) {
        EventResponseDTO responseDTO = eventService.createEvent(requestDTO, request);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Retrieve all accessible events",
            description = "Returns PUBLIC events + PRIVATE events you organized + PRIVATE events you're invited to. Paginated results."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of events"),
            @ApiResponse(responseCode = "403", description = "Access denied - Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<EventResponseDTO> events = eventService.getAllEvents(page, size);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Retrieve an event by ID",
            description = "Returns event details if: PUBLIC event OR you're the organizer OR you're invited. Returns 403 if you don't have access to PRIVATE event."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the event"),
            @ApiResponse(responseCode = "403", description = "Access denied - You cannot view this private event"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "ID of the event to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        EventResponseDTO responseDTO = eventService.getEventById(id);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Update an existing event",
            description = "Only the event organizer can update the event. Cannot update past events."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only organizer can update"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "ID of the event to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO requestDTO,
            HttpServletRequest request) {
        EventResponseDTO responseDTO = eventService.updateEvent(id, requestDTO, request);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Delete an event",
            description = "Only the event organizer can delete the event. Cannot delete past or ongoing events."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only organizer can delete"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(
            @Parameter(description = "ID of the event to delete", required = true, example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        eventService.deleteEvent(id, request);
        return new ResponseEntity<>("Event deleted successfully", HttpStatus.OK);
    }

    // ========== NEW ENDPOINTS FOR PUBLIC/PRIVATE EVENTS ==========

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get all PUBLIC events",
            description = "Returns only PUBLIC events that anyone can view and attend."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved public events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/public")
    public ResponseEntity<List<EventResponseDTO>> getAllPublicEvents() {
        List<EventResponseDTO> events = eventService.getAllPublicEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get my organized events",
            description = "Returns all events created by the current user (both PUBLIC and PRIVATE)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved organized events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/my-organized")
    public ResponseEntity<List<EventResponseDTO>> getMyOrganizedEvents() {
        List<EventResponseDTO> events = eventService.getMyOrganizedEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get events I'm invited to",
            description = "Returns all PRIVATE events where the current user is invited."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invited events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/my-invitations")
    public ResponseEntity<List<EventResponseDTO>> getMyInvitedEvents() {
        List<EventResponseDTO> events = eventService.getMyInvitedEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get upcoming events",
            description = "Returns upcoming PUBLIC events + your PRIVATE events (organized or invited)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved upcoming events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDTO>> getUpcomingEvents() {
        List<EventResponseDTO> events = eventService.getUpcomingEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get past events",
            description = "Returns all past events."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved past events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/past")
    public ResponseEntity<List<EventResponseDTO>> getPastEvents() {
        List<EventResponseDTO> events = eventService.getPastEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get today's events",
            description = "Returns all events happening today."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved today's events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/today")
    public ResponseEntity<List<EventResponseDTO>> getTodaysEvents() {
        List<EventResponseDTO> events = eventService.getTodaysEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Get events by location",
            description = "Search events by location (case-insensitive, partial match)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search/location")
    public ResponseEntity<List<EventResponseDTO>> getEventsByLocation(
            @Parameter(description = "Location to search", example = "Dhaka")
            @RequestParam String location) {
        List<EventResponseDTO> events = eventService.getEventsByLocation(location);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    // ========== INVITATION MANAGEMENT ENDPOINTS ==========

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Invite users to a PRIVATE event",
            description = "Only the event organizer can invite users. Only works for PRIVATE events."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users invited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or trying to invite to PUBLIC event"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only organizer can invite"),
            @ApiResponse(responseCode = "404", description = "Event or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/invite")
    public ResponseEntity<EventResponseDTO> inviteUsersToEvent(
            @Valid @RequestBody EventInvitationDTO invitationDTO,
            HttpServletRequest request) {
        EventResponseDTO responseDTO = eventService.inviteUsersToEvent(invitationDTO, request);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ATTENDEE')")
    @Operation(
            summary = "Remove users from a PRIVATE event",
            description = "Only the event organizer can remove invited users. Only works for PRIVATE events."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or trying to remove from PUBLIC event"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only organizer can remove"),
            @ApiResponse(responseCode = "404", description = "Event or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/invite")
    public ResponseEntity<EventResponseDTO> removeUsersFromEvent(
            @Valid @RequestBody EventInvitationDTO invitationDTO,
            HttpServletRequest request) {
        EventResponseDTO responseDTO = eventService.removeUsersFromEvent(invitationDTO, request);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
