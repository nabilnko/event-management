package com.example.eventmanagement.model;

import com.example.eventmanagement.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @NotBlank(message = "Event title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Event date is required")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    // NEW: Event Type (PUBLIC or PRIVATE)
    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType = EventType.PUBLIC; // Default to PUBLIC

    // NEW: Event organizer (who created the event)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // NEW: Invited users (for PRIVATE events only)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "event_invitations",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> invitedUsers = new HashSet<>();

    // Constructors
    public Event() {
    }

    public Event(String title, String description, LocalDate eventDate,
                 LocalTime startTime, LocalTime endTime, String location) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public Event(String title, String description, LocalDate eventDate,
                 LocalTime startTime, LocalTime endTime, String location,
                 EventType eventType, User organizer) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.eventType = eventType;
        this.organizer = organizer;
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

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Set<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(Set<User> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    // Helper method: Add invited user
    public void addInvitedUser(User user) {
        this.invitedUsers.add(user);
    }

    // Helper method: Remove invited user
    public void removeInvitedUser(User user) {
        this.invitedUsers.remove(user);
    }

    // Helper method: Check if user is invited
    public boolean isUserInvited(User user) {
        return this.invitedUsers.stream()
                .anyMatch(invitedUser -> invitedUser.getId().equals(user.getId()));
    }

    // Helper method: Check if user is organizer
    public boolean isOrganizer(User user) {
        return this.organizer != null && this.organizer.getId().equals(user.getId());
    }

    // Helper method: Check if user can access this event
    public boolean canUserAccess(User user) {
        // Public events: everyone can access
        if (this.eventType == EventType.PUBLIC) {
            return true;
        }

        // Private events: only organizer and invited users
        return isOrganizer(user) || isUserInvited(user);
    }
}
