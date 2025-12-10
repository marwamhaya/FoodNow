package com.example.foodNow.service;

import com.example.foodNow.model.Notification;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendNotification(User recipient, String title, String message, Notification.NotificationType type) {
        // Save to DB
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);

        // Mock Push Notification (FCM)
        log.info("Sending PUSH to user {}: {} - {}", recipient.getId(), title, message);

        // Mock Email if needed
        if (type == Notification.NotificationType.SYSTEM) {
            log.info("Sending EMAIL to user {}: {} - {}", recipient.getEmail(), title, message);
        }
    }
}
