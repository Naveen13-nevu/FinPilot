package com.finpilot.service.impl;

import com.finpilot.dto.response.NotificationResponse;
import com.finpilot.entity.Notification;
import com.finpilot.entity.NotificationType;
import com.finpilot.entity.User;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.NotificationMapper;
import com.finpilot.repository.NotificationRepository;
import com.finpilot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void notify(User user, NotificationType type, String title, String message, UUID relatedEntityId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getAll(User currentUser) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(User currentUser) {
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(User currentUser, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUser(notificationId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        notification.setIsRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(User currentUser) {
        notificationRepository.markAllAsRead(currentUser);
    }
}