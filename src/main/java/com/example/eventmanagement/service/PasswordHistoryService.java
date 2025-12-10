package com.example.eventmanagement.service;

import com.example.eventmanagement.model.UserPasswordHistory;
import com.example.eventmanagement.repository.UserPasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PasswordHistoryService {

    private final UserPasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    public PasswordHistoryService(UserPasswordHistoryRepository passwordHistoryRepository) {
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    public void recordPasswordChange(String userId, String changedBy, String oldPassword, String newPassword) {
        UserPasswordHistory history = new UserPasswordHistory();
        history.setUserId(userId);
        history.setPasswordChangeBy(changedBy);
        history.setChangeDate(LocalDateTime.now());
        history.setOldPassword(oldPassword);
        history.setNewPassword(newPassword);
        history.setCreatedBy(changedBy);
        history.setIsActive(true);

        passwordHistoryRepository.save(history);
    }

    public List<UserPasswordHistory> getUserPasswordHistory(String userId) {
        return passwordHistoryRepository.findByUserIdOrderByChangeDateDesc(userId);
    }
}
