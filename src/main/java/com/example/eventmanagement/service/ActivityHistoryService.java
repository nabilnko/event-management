package com.example.eventmanagement.service;

import com.example.eventmanagement.model.UserActivityHistory;
import com.example.eventmanagement.enums.ActivityType;
import com.example.eventmanagement.repository.UserActivityHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityHistoryService {

    private final UserActivityHistoryRepository activityHistoryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ActivityHistoryService(UserActivityHistoryRepository activityHistoryRepository) {
        this.activityHistoryRepository = activityHistoryRepository;
        this.objectMapper = new ObjectMapper();
    }

    // Method 1: Original - with userId, username, role parameters
    public void recordActivity(ActivityType activityType, String userId, String username,
                               String userRole, HttpServletRequest request) {
        UserActivityHistory history = new UserActivityHistory();
        history.setUserId(userId);
        history.setUsername(username);
        history.setUserGroup(userRole);
        history.setActivityTypeName(activityType.getDescription());
        history.setActivityTypeCode(activityType.getCode());
        history.setActivityDate(LocalDateTime.now());
        history.setIp(getClientIp(request));
        history.setDeviceId(getDeviceId(request));
        history.setSessionId(request.getSession().getId());
        history.setCreatedBy(username);
        history.setIsActive(true);

        activityHistoryRepository.save(history);
    }

    // Method 2: Simple - auto-detect user from SecurityContext
    public void recordActivity(ActivityType activityType, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");

            recordActivity(activityType, username, username, role, request);
        }
    }

    // Method 3: NEW - Enhanced with entity details (6 parameters)
    public void recordActivity(ActivityType activityType, HttpServletRequest request,
                               String entityType, String entityId, String entityName, String description) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");

            UserActivityHistory history = new UserActivityHistory();
            history.setUserId(username);
            history.setUsername(username);
            history.setUserGroup(role);
            history.setActivityTypeName(activityType.getDescription());
            history.setActivityTypeCode(activityType.getCode());
            history.setActivityDate(LocalDateTime.now());
            history.setIp(getClientIp(request));
            history.setDeviceId(getDeviceId(request));
            history.setSessionId(request.getSession().getId());
            history.setCreatedBy(username);
            history.setIsActive(true);

            // Set entity details
            history.setEntityType(entityType);
            history.setEntityId(entityId);
            history.setEntityName(entityName);
            history.setDescription(description);

            activityHistoryRepository.save(history);
        }
    }

    // Method 4: NEW - Full with old/new values (8 parameters)
    public void recordActivity(ActivityType activityType, HttpServletRequest request,
                               String entityType, String entityId, String entityName,
                               String description, Object oldValues, Object newValues) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");

            UserActivityHistory history = new UserActivityHistory();
            history.setUserId(username);
            history.setUsername(username);
            history.setUserGroup(role);
            history.setActivityTypeName(activityType.getDescription());
            history.setActivityTypeCode(activityType.getCode());
            history.setActivityDate(LocalDateTime.now());
            history.setIp(getClientIp(request));
            history.setDeviceId(getDeviceId(request));
            history.setSessionId(request.getSession().getId());
            history.setCreatedBy(username);
            history.setIsActive(true);

            // Set entity details
            history.setEntityType(entityType);
            history.setEntityId(entityId);
            history.setEntityName(entityName);
            history.setDescription(description);

            // Convert old/new values to JSON
            try {
                if (oldValues != null) {
                    history.setOldValues(objectMapper.writeValueAsString(oldValues));
                }
                if (newValues != null) {
                    history.setNewValues(objectMapper.writeValueAsString(newValues));
                }
            } catch (Exception e) {
                System.err.println("Error serializing values: " + e.getMessage());
            }

            activityHistoryRepository.save(history);
        }
    }

    public List<UserActivityHistory> getUserActivities(String userId) {
        return activityHistoryRepository.findByUserIdOrderByActivityDateDesc(userId);
    }

    public List<UserActivityHistory> getActivitiesByType(String activityTypeCode) {
        return activityHistoryRepository.findByActivityTypeCodeOrderByActivityDateDesc(activityTypeCode);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "Unknown";
    }

    private String getDeviceId(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 255)) : "Unknown";
    }
}
