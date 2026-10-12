package com.basketly.notification.service;

import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.notification.domain.Notification;
import com.basketly.notification.dto.NotificationDto;
import com.basketly.notification.repository.NotificationRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markRead(User user, UUID id) {
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepository.markAllReadForUser(user);
    }

    @Transactional
    public Notification createNotification(User user, String title, String message, String type, String deepLink) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .deepLink(deepLink)
                .read(false)
                .build();
        return notificationRepository.save(notification);
    }
}
