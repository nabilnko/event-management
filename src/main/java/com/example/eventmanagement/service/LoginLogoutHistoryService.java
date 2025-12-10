package com.example.eventmanagement.service;

import com.example.eventmanagement.model.UserLoginLogoutHistory;
import com.example.eventmanagement.repository.UserLoginLogoutHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginLogoutHistoryService {

    private final UserLoginLogoutHistoryRepository loginLogoutHistoryRepository;

    @Autowired
    public LoginLogoutHistoryService(UserLoginLogoutHistoryRepository loginLogoutHistoryRepository) {
        this.loginLogoutHistoryRepository = loginLogoutHistoryRepository;
    }

    public void recordLogin(String userId, String username, String role, String token,
                            HttpServletRequest request, String status) {
        UserLoginLogoutHistory history = new UserLoginLogoutHistory();
        history.setUserId(userId);
        history.setUserToken(token);
        history.setUserType(role);
        history.setRequestFrom(getUserAgent(request));
        history.setRequestIp(getClientIp(request));
        history.setDeviceInfo(getDeviceInfo(request));
        history.setLoginTime(LocalDateTime.now());
        history.setCreatedBy(username);
        history.setIsActive(true);
        history.setLoginStatus(status);

        loginLogoutHistoryRepository.save(history);
    }

    public void recordLogout(String token) {
        loginLogoutHistoryRepository.findByUserTokenAndLogoutTimeIsNull(token)
                .ifPresent(history -> {
                    history.setLogoutTime(LocalDateTime.now());
                    loginLogoutHistoryRepository.save(history);
                });
    }

    public List<UserLoginLogoutHistory> getUserLoginHistory(String userId) {
        return loginLogoutHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId);
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
        return ip;
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";

        if (userAgent.contains("Mobile")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    // Add this method
    public List<UserLoginLogoutHistory> getUserLoginHistoryByUsername(String username) {
        // You need to get userId from username first
        // This requires injecting UserRepository
        return loginLogoutHistoryRepository.findByUserIdOrderByLoginTimeDesc(username);
    }

}
