package com.example.notification.service;

import com.example.notification.domain.Notification;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer para envio de notificações.
 * Em produção: integração com provedores reais (SendGrid, Twilio, Firebase).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository repository;
    
    public Notification sendNotification(
            String recipientId,
            Notification.Channel channel,
            Notification.Priority priority,
            String templateId,
            Map<String, String> templateVars,
            String emailAddress,
            String phoneNumber,
            String deviceToken) {
        
        log.info("Sending notification: recipientId={}, channel={}, templateId={}", 
                recipientId, channel, templateId);
        
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .recipientId(recipientId)
                .channel(channel)
                .priority(priority)
                .templateId(templateId)
                .templateVars(templateVars)
                .emailAddress(emailAddress)
                .phoneNumber(phoneNumber)
                .deviceToken(deviceToken)
                .status(Notification.Status.QUEUED)
                .sentAt(Instant.now())
                .build();
        
        // Simula envio (em produção: chamada ao provedor)
        boolean success = simulateSend(notification);
        
        notification.setStatus(success ? Notification.Status.SENT : Notification.Status.FAILED);
        if (success) {
            notification.setDeliveredAt(Instant.now());
        }
        
        repository.save(notification);
        
        log.info("Notification processed: notificationId={}, status={}", 
                notification.getNotificationId(), notification.getStatus());
        
        return notification;
    }
    
    public Notification getNotification(String notificationId) {
        return repository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
    }
    
    /**
     * Simula envio com taxa de sucesso de 95%.
     * Em produção: integração real com provedores.
     */
    private boolean simulateSend(Notification notification) {
        // Simula latência de rede
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 95% de sucesso
        return Math.random() < 0.95;
    }
}
