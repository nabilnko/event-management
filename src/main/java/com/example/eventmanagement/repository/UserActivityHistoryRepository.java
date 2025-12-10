package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.UserActivityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityHistoryRepository extends JpaRepository<UserActivityHistory, Long> {

    List<UserActivityHistory> findByUserIdOrderByActivityDateDesc(String userId);

    List<UserActivityHistory> findByActivityTypeCodeOrderByActivityDateDesc(String activityTypeCode);

    List<UserActivityHistory> findByUserGroupOrderByActivityDateDesc(String userGroup);

    @Query("SELECT h FROM UserActivityHistory h WHERE h.activityDate BETWEEN :startDate AND :endDate ORDER BY h.activityDate DESC")
    List<UserActivityHistory> findByActivityDateBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}
