package com.company.fyp_management.controller;

import com.company.fyp_management.entity.Notification;
import com.company.fyp_management.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Helper to get userId and userType from session
    private Integer getUserIdFromSession(HttpSession session) {
        Object uid = session.getAttribute("userId");
        if (uid == null) {
            throw new IllegalArgumentException("No authenticated user in session");
        }
        if (uid instanceof Number) {
            return ((Number) uid).intValue();
        }
        return Integer.parseInt(uid.toString());
    }

    private String getUserTypeFromSession(HttpSession session) {
        Object role = session.getAttribute("role");
        if (role == null) {
            throw new IllegalArgumentException("No role in session");
        }
        String roleStr = role.toString().toLowerCase();
        if (roleStr.equals("student")) {
            return "student";
        }
        return "faculty";
    }

    // Get all notifications for the logged-in user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Notification> getNotifications(HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        String userType = getUserTypeFromSession(session);
        return notificationService.getNotifications(userId, userType);
    }

    // Get only unread notifications
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public List<Notification> getUnreadNotifications(HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        String userType = getUserTypeFromSession(session);
        return notificationService.getUnreadNotifications(userId, userType);
    }

    // Get count of unread notifications
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> getUnreadCount(HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        String userType = getUserTypeFromSession(session);
        Long count = notificationService.getUnreadCount(userId, userType);
        return Map.of("count", count);
    }

    // Mark a single notification as read
    @PostMapping("/mark-read/{id}")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> markAsRead(@PathVariable("id") Integer id) {
        notificationService.markAsRead(id);
        return Map.of("message", "Notification marked as read");
    }

    // Mark all notifications as read
    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> markAllAsRead(HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        String userType = getUserTypeFromSession(session);
        notificationService.markAllAsRead(userId, userType);
        return Map.of("message", "All notifications marked as read");
    }
}
