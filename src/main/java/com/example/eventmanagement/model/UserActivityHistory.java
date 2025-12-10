package com.example.eventmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_group")
    private String userGroup;  // Role name

    @Column(name = "activity_type_name")
    private String activityTypeName;  // "Create User", "Delete Event", etc.

    @Column(name = "activity_type_code")
    private String activityTypeCode;  // "USER_CREATE", "EVENT_DELETE", etc.

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "username")
    private String username;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "activity_date")
    private LocalDateTime activityDate;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "ip")
    private String ip;

    @Column(name = "session_id")
    private String sessionId;


    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "entity_name", length = 500)
    private String entityName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;
}
