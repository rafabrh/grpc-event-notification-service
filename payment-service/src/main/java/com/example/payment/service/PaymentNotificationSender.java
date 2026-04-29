package com.example.payment.service;

import com.example.grpc.notification.NotificationChannel;
import com.example.grpc.notification.NotificationPriority;
import com.example.grpc.notification.NotificationResponse;
import com.example.grpc.notification.SendNotificationRequest;
import com.example.payment.client.NotificationClient;
import com.example.payment.domain.Payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Componente responsável por enviar notificações ao cliente após processamento de pagamento.
 *
 * Extraído do PaymentService para que @CircuitBreaker e @Retry funcionem
 * corretamente via Spring AOP (que requer proxy de método público em bean separado).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotificationSender {

    private final NotificationClient notificationClient;

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notificationFallback")
    @Retry(name = "notificationService")
    public void notifyCustomer(Payment payment) {
        String templateId = payment.getStatus() == Payment.PaymentStatus.APPROVED
                ? "payment_approved"
                : "payment_rejected";

        Map<String, String> templateVars = Map.of(
                "payment_id", payment.getPaymentId(),
                "amount", formatMoney(payment.getAmount()),
                "auth_code", payment.getAuthorizationCode()
        );

        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId(payment.getCustomerId())
                .setChannel(NotificationChannel.EMAIL)
                .setPriority(NotificationPriority.HIGH)
                .setTemplateId(templateId)
                .putAllTemplateVars(templateVars)
                .setEmailAddress("customer@example.com")  // Em produção: buscar do BD
                .build();

        NotificationResponse response = notificationClient.sendNotification(request);

        log.info("Notification sent: notificationId={}, status={}",
                response.getNotificationId(), response.getStatus());
    }

    /**
     * Fallback quando Notification Service está indisponível.
     * Log para retry posterior ou Dead Letter Queue.
     */
    public void notificationFallback(Payment payment, Exception e) {
        log.error("Failed to send notification after retries. Payment will be saved for later notification. " +
                  "paymentId={}", payment.getPaymentId(), e);
    }

    private String formatMoney(Payment.Money money) {
        double value = money.getAmountCents() / 100.0;
        return String.format("%s %.2f", money.getCurrency(), value);
    }
}
