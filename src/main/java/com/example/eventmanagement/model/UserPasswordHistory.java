package com.example.eventmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_password_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "password_change_by", nullable = false, length = 100)
    private String passwordChangeBy;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(name = "old_password", length = 200)
    private String oldPassword;

    @Column(name = "new_password", length = 200)
    private String newPassword;

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
}
