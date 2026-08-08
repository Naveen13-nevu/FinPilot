package com.finpilot.service;

import com.finpilot.dto.response.NotificationResponse;
import com.finpilot.entity.NotificationType;
import com.finpilot.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void notify(User user, NotificationType type, String title, String message, UUID relatedEntityId);

    List<NotificationResponse> getAll(User currentUser);

    long getUnreadCount(User currentUser);

    NotificationResponse markAsRead(User currentUser, UUID notificationId);

    void markAllAsRead(User currentUser);
}