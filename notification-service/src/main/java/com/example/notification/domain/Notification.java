package com.example.notification.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class Notification {
    
    private String notificationId;
    private String recipientId;
    private Channel channel;
    private Priority priority;
    private Status status;
    private String templateId;
    private Map<String, String> templateVars;
    private String emailAddress;
    private String phoneNumber;
    private String deviceToken;
    private Instant sentAt;
    private Instant deliveredAt;
    
    public enum Channel {
        EMAIL, SMS, PUSH, WHATSAPP
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum Status {
        QUEUED, SENT, DELIVERED, FAILED
    }
}
