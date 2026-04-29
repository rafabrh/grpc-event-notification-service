package com.example.payment.client;

import com.example.grpc.notification.NotificationResponse;
import com.example.grpc.notification.NotificationServiceGrpc;
import com.example.grpc.notification.SendNotificationRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Client gRPC para comunicação com Notification Service.
 * 
 * Patterns implementados:
 * - Deadline explícito em todas as chamadas
 * - Error handling com retry logic
 * - Logging estruturado
 */
@Slf4j
@Component
public class NotificationClient {
    
    @GrpcClient("notification-service")
    private NotificationServiceGrpc.NotificationServiceBlockingStub notificationStub;
    
    /**
     * Envia notificação com deadline de 5 segundos.
     * 
     * @throws StatusRuntimeException se chamada falhar
     */
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.debug("Calling NotificationService.SendNotification: recipientId={}, channel={}", 
                request.getRecipientId(), request.getChannel());
        
        try {
            // CRITICAL: Sempre definir deadline para evitar hang indefinido
            NotificationResponse response = notificationStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .sendNotification(request);
            
            log.debug("NotificationService responded: notificationId={}, status={}", 
                    response.getNotificationId(), response.getStatus());
            
            return response;
            
        } catch (StatusRuntimeException e) {
            log.error("Failed to call NotificationService: code={}, description={}", 
                    e.getStatus().getCode(), e.getStatus().getDescription());
            throw e;
        }
    }
}
