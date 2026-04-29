package com.example.notification.repository;

import com.example.notification.domain.Notification;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NotificationRepository {
    
    private final Map<String, Notification> database = new ConcurrentHashMap<>();
    
    public Notification save(Notification notification) {
        database.put(notification.getNotificationId(), notification);
        return notification;
    }
    
    public Optional<Notification> findById(String notificationId) {
        return Optional.ofNullable(database.get(notificationId));
    }
}
