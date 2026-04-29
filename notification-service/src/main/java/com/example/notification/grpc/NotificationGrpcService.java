package com.example.notification.grpc;

import com.example.grpc.common.ResponseMetadata;
import com.example.grpc.notification.*;
import com.example.notification.domain.Notification;
import com.example.notification.service.NotificationService;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {
    
    private final NotificationService notificationService;
    
    @Override
    public void sendNotification(SendNotificationRequest request, 
                                  StreamObserver<NotificationResponse> responseObserver) {
        log.info("gRPC SendNotification called: recipientId={}, channel={}", 
                request.getRecipientId(), request.getChannel());
        
        try {
            validateRequest(request);
            
            Notification notification = notificationService.sendNotification(
                    request.getRecipientId(),
                    mapChannel(request.getChannel()),
                    mapPriority(request.getPriority()),
                    request.getTemplateId(),
                    request.getTemplateVarsMap(),
                    request.getEmailAddress(),
                    request.getPhoneNumber(),
                    request.getDeviceToken()
            );
            
            NotificationResponse response = toProto(notification);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid notification request: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
                    
        } catch (Exception e) {
            log.error("Error sending notification", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getNotification(GetNotificationRequest request, 
                                 StreamObserver<NotificationResponse> responseObserver) {
        log.info("gRPC GetNotification called: notificationId={}", request.getNotificationId());
        
        try {
            Notification notification = notificationService.getNotification(request.getNotificationId());
            
            NotificationResponse response = toProto(notification);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
                    
        } catch (Exception e) {
            log.error("Error getting notification", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
    
    /**
     * Client Streaming: recebe múltiplas notificações e retorna sumário.
     */
    @Override
    public StreamObserver<SendNotificationRequest> batchSend(
            StreamObserver<BatchSendResponse> responseObserver) {
        
        log.info("gRPC BatchSend stream started");
        
        return new StreamObserver<>() {
            private final List<NotificationResponse> results = new ArrayList<>();
            private int totalSent = 0;
            private int totalFailed = 0;
            
            @Override
            public void onNext(SendNotificationRequest request) {
                try {
                    Notification notification = notificationService.sendNotification(
                            request.getRecipientId(),
                            mapChannel(request.getChannel()),
                            mapPriority(request.getPriority()),
                            request.getTemplateId(),
                            request.getTemplateVarsMap(),
                            request.getEmailAddress(),
                            request.getPhoneNumber(),
                            request.getDeviceToken()
                    );
                    
                    results.add(toProto(notification));
                    
                    if (notification.getStatus() == Notification.Status.SENT) {
                        totalSent++;
                    } else {
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("Error in batch send", e);
                    totalFailed++;
                }
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error in batch send stream", t);
            }
            
            @Override
            public void onCompleted() {
                BatchSendResponse response = BatchSendResponse.newBuilder()
                        .addAllResults(results)
                        .setTotalSent(totalSent)
                        .setTotalFailed(totalFailed)
                        .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.info("BatchSend completed: sent={}, failed={}", totalSent, totalFailed);
            }
        };
    }
    
    /**
     * Bidirecional Streaming: monitoramento em tempo real.
     * Cliente envia IDs, servidor stream status updates.
     */
    @Override
    public StreamObserver<GetNotificationRequest> monitorNotifications(
            StreamObserver<NotificationResponse> responseObserver) {
        
        log.info("gRPC MonitorNotifications stream started");
        
        return new StreamObserver<>() {
            @Override
            public void onNext(GetNotificationRequest request) {
                try {
                    Notification notification = notificationService.getNotification(
                            request.getNotificationId()
                    );
                    
                    // Envia status atual
                    responseObserver.onNext(toProto(notification));
                    
                } catch (Exception e) {
                    log.warn("Notification not found in monitor: {}", request.getNotificationId());
                }
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error in monitor stream", t);
            }
            
            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                log.info("MonitorNotifications stream completed");
            }
        };
    }
    
    // Helpers
    
    private void validateRequest(SendNotificationRequest request) {
        if (request.getRecipientId() == null || request.getRecipientId().isBlank()) {
            throw new IllegalArgumentException("recipient_id is required");
        }
        if (request.getChannel() == NotificationChannel.NOTIFICATION_CHANNEL_UNSPECIFIED) {
            throw new IllegalArgumentException("channel is required");
        }
        if (request.getTemplateId() == null || request.getTemplateId().isBlank()) {
            throw new IllegalArgumentException("template_id is required");
        }
    }
    
    private Notification.Channel mapChannel(NotificationChannel proto) {
        return switch (proto) {
            case EMAIL -> Notification.Channel.EMAIL;
            case SMS -> Notification.Channel.SMS;
            case PUSH -> Notification.Channel.PUSH;
            case WHATSAPP -> Notification.Channel.WHATSAPP;
            default -> throw new IllegalArgumentException("Invalid channel");
        };
    }
    
    private Notification.Priority mapPriority(NotificationPriority proto) {
        return switch (proto) {
            case LOW -> Notification.Priority.LOW;
            case MEDIUM -> Notification.Priority.MEDIUM;
            case HIGH -> Notification.Priority.HIGH;
            case URGENT -> Notification.Priority.URGENT;
            default -> Notification.Priority.MEDIUM;
        };
    }
    
    private NotificationStatus mapStatus(Notification.Status domain) {
        return switch (domain) {
            case QUEUED -> NotificationStatus.QUEUED;
            case SENT -> NotificationStatus.SENT;
            case DELIVERED -> NotificationStatus.DELIVERED;
            case FAILED -> NotificationStatus.FAILED;
        };
    }
    
    private NotificationResponse toProto(Notification notification) {
        return NotificationResponse.newBuilder()
                .setNotificationId(notification.getNotificationId())
                .setStatus(mapStatus(notification.getStatus()))
                .setSentAt(toTimestamp(notification.getSentAt()))
                .setMetadata(buildMetadata())
                .build();
    }
    
    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
    
    private ResponseMetadata buildMetadata() {
        return ResponseMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setTimestamp(toTimestamp(Instant.now()))
                .setServiceVersion("1.0.0")
                .build();
    }
}
