package com.company.fyp_management.service;

import com.company.fyp_management.entity.Notification;
import com.company.fyp_management.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Create a new notification
    @Transactional
    public Notification createNotification(Integer userId, String userType, String title, String message, String type, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    // Get all notifications for a user
    public List<Notification> getNotifications(Integer userId, String userType) {
        return notificationRepository.findAllByUserIdAndUserTypeOrderByCreatedAtDesc(userId, userType);
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications(Integer userId, String userType) {
        return notificationRepository.findUnreadByUserIdAndUserType(userId, userType);
    }

    // Get unread count
    public Long getUnreadCount(Integer userId, String userType) {
        return notificationRepository.countUnreadByUserIdAndUserType(userId, userType);
    }

    // Mark single notification as read
    @Transactional
    public void markAsRead(Integer notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    // Mark all notifications as read
    @Transactional
    public void markAllAsRead(Integer userId, String userType) {
        notificationRepository.markAllAsReadByUserIdAndUserType(userId, userType);
    }

    // Helper methods to create specific notification types
    
    public void notifyStudentFeedback(Integer studentId, String docType, String feedbackContent) {
        String title = "New Feedback Received";
        String message = "Your supervisor has provided feedback on your " + docType + ": \"" + 
            (feedbackContent.length() > 50 ? feedbackContent.substring(0, 50) + "..." : feedbackContent) + "\"";
        createNotification(studentId, "student", title, message, "feedback", null);
    }

    public void notifyStudentApproval(Integer studentId, String docType) {
        String title = "Submission Approved! ✓";
        String message = "Great news! Your " + docType + " has been approved by your supervisor.";
        createNotification(studentId, "student", title, message, "approval", null);
    }

    public void notifyStudentRejection(Integer studentId, String docType) {
        String title = "Revision Requested";
        String message = "Your supervisor has requested a revision for your " + docType + ". Please review and resubmit.";
        createNotification(studentId, "student", title, message, "rejection", null);
    }

    public void notifyStudentGradesReleased(Integer studentId) {
        String title = "Grades Released! 📊";
        String message = "Your FYP grades have been released. Check your grades section to view them.";
        createNotification(studentId, "student", title, message, "grade", null);
    }

    public void notifySupervisorNewSubmission(Integer facultyId, String studentName, String docType) {
        String title = "New Submission";
        String message = studentName + " has submitted their " + docType + ". Please review it.";
        createNotification(facultyId, "faculty", title, message, "submission", null);
    }
}
