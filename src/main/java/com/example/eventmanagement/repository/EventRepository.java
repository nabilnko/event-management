package com.example.eventmanagement.repository;

import com.example.eventmanagement.enums.EventType;
import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // ========== EXISTING METHODS (Your original code) ==========

    // Check if event title already exists (for duplicate validation)
    boolean existsByTitle(String title);

    // Find event by title
    Optional<Event> findByTitle(String title);

    // Find upcoming events (event date in the future)
    List<Event> findByEventDateAfter(LocalDate date);

    // Find past events (event date in the past)
    List<Event> findByEventDateBefore(LocalDate date);

    // Find events on a specific date (e.g., today's events)
    List<Event> findByEventDate(LocalDate date);

    // Find events by location (case-insensitive, partial match)
    List<Event> findByLocationContainingIgnoreCase(String location);

    // Find events within a date range
    List<Event> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    // Find events ordered by date and start time
    List<Event> findAllByOrderByEventDateAscStartTimeAsc();

    // ========== NEW METHODS FOR PUBLIC/PRIVATE EVENTS ==========

    // Find events by type (PUBLIC or PRIVATE)
    List<Event> findByEventType(EventType eventType);

    // Find all PUBLIC events
    @Query("SELECT e FROM Event e WHERE e.eventType = 'PUBLIC' ORDER BY e.eventDate ASC, e.startTime ASC")
    List<Event> findAllPublicEvents();

    // Find events organized by a specific user
    List<Event> findByOrganizer(User organizer);

    // Find events organized by user ID
    List<Event> findByOrganizerId(Long organizerId);

    // Find events where user is invited
    @Query("SELECT e FROM Event e JOIN e.invitedUsers u WHERE u.id = :userId")
    List<Event> findEventsByInvitedUserId(@Param("userId") Long userId);

    // Find events user can access (public + organized by user + invited to)
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.invitedUsers u " +
            "WHERE e.eventType = 'PUBLIC' OR e.organizer.id = :userId OR u.id = :userId " +
            "ORDER BY e.eventDate ASC, e.startTime ASC")
    List<Event> findAccessibleEventsByUserId(@Param("userId") Long userId);

    // Find upcoming public events
    @Query("SELECT e FROM Event e WHERE e.eventType = 'PUBLIC' AND e.eventDate >= :date " +
            "ORDER BY e.eventDate ASC, e.startTime ASC")
    List<Event> findUpcomingPublicEvents(@Param("date") LocalDate date);

    // Find upcoming events for a user (public + their private events)
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.invitedUsers u " +
            "WHERE e.eventDate >= :date AND " +
            "(e.eventType = 'PUBLIC' OR e.organizer.id = :userId OR u.id = :userId) " +
            "ORDER BY e.eventDate ASC, e.startTime ASC")
    List<Event> findUpcomingEventsForUser(@Param("userId") Long userId, @Param("date") LocalDate date);
}
