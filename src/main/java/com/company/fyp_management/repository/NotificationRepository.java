package com.company.fyp_management.repository;

import com.company.fyp_management.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Get all notifications for a user, ordered by creation date (newest first)
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType ORDER BY n.createdAt DESC")
    List<Notification> findAllByUserIdAndUserTypeOrderByCreatedAtDesc(
        @Param("userId") Integer userId, 
        @Param("userType") String userType
    );

    // Get unread notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserIdAndUserType(
        @Param("userId") Integer userId, 
        @Param("userType") String userType
    );

    // Count unread notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.userType = :userType AND n.isRead = false")
    Long countUnreadByUserIdAndUserType(
        @Param("userId") Integer userId, 
        @Param("userType") String userType
    );

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.userType = :userType")
    void markAllAsReadByUserIdAndUserType(
        @Param("userId") Integer userId, 
        @Param("userType") String userType
    );

    // Mark a single notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Integer id);
}
