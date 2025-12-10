package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.UserPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, Long> {

    List<UserPasswordHistory> findByUserIdOrderByChangeDateDesc(String userId);

    List<UserPasswordHistory> findByPasswordChangeByOrderByChangeDateDesc(String passwordChangeBy);
}
