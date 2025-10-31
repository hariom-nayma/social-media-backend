package com.socialmedia.app.service;

import com.socialmedia.app.dto.NotificationDto;
import com.socialmedia.app.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    NotificationDto pushNotification(String toUsername, NotificationType type,
                                     String message, String payload);
    List<NotificationDto> fetchRecent(String username);
    void markAsRead(String notificationId);
    List<NotificationDto> getAllNotifications();
}
