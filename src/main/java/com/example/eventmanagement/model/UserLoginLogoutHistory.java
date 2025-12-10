package com.example.eventmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_logout_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginLogoutHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_token", nullable = false, columnDefinition = "TEXT")
    private String userToken;

    @Column(name = "user_type", nullable = false, length = 100)
    private String userType;

    @Column(name = "request_from", length = 100)
    private String requestFrom;

    @Column(name = "request_ip", nullable = false, columnDefinition = "TEXT")
    private String requestIp;

    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "login_status", length = 255)
    private String loginStatus;
}
