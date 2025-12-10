package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.UserLoginLogoutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoginLogoutHistoryRepository extends JpaRepository<UserLoginLogoutHistory, Long> {

    List<UserLoginLogoutHistory> findByUserIdOrderByLoginTimeDesc(String userId);

    List<UserLoginLogoutHistory> findByLoginStatusOrderByLoginTimeDesc(String loginStatus);

    Optional<UserLoginLogoutHistory> findByUserTokenAndLogoutTimeIsNull(String userToken);

    @Query("SELECT h FROM UserLoginLogoutHistory h WHERE h.loginTime BETWEEN :startDate AND :endDate ORDER BY h.loginTime DESC")
    List<UserLoginLogoutHistory> findByLoginTimeBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}
