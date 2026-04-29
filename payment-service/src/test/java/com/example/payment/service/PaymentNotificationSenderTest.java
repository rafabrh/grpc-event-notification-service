package com.example.payment.service;

import com.example.grpc.notification.NotificationResponse;
import com.example.grpc.notification.NotificationStatus;
import com.example.grpc.notification.SendNotificationRequest;
import com.example.payment.client.NotificationClient;
import com.example.payment.domain.Payment;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationSenderTest {

    @Mock
    private NotificationClient notificationClient;

    private PaymentNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new PaymentNotificationSender(notificationClient);
    }

    private Payment createPayment(Payment.PaymentStatus status) {
        return Payment.builder()
                .paymentId("PAY-001")
                .customerId("CUST-001")
                .amount(Payment.Money.builder().currency("BRL").amountCents(10000L).build())
                .authorizationCode("ABC12345")
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldSendApprovedNotification() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-001")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        sender.notifyCustomer(createPayment(Payment.PaymentStatus.APPROVED));

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        SendNotificationRequest request = captor.getValue();
        assertThat(request.getRecipientId()).isEqualTo("CUST-001");
        assertThat(request.getTemplateId()).isEqualTo("payment_approved");
        assertThat(request.getTemplateVarsMap()).containsEntry("payment_id", "PAY-001");
    }

    @Test
    void shouldSendRejectedNotification() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-002")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        sender.notifyCustomer(createPayment(Payment.PaymentStatus.REJECTED));

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        assertThat(captor.getValue().getTemplateId()).isEqualTo("payment_rejected");
    }

    @Test
    void shouldFallbackGracefully() {
        Payment payment = createPayment(Payment.PaymentStatus.APPROVED);
        Exception cause = new StatusRuntimeException(Status.UNAVAILABLE);

        assertThatCode(() -> sender.notificationFallback(payment, cause))
                .doesNotThrowAnyException();
    }

    // === EDGE CASES — Cenários de produção ===

    @Test
    void shouldFormatMoneyCorrectly() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-003")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        sender.notifyCustomer(createPayment(Payment.PaymentStatus.APPROVED));

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        // 10000 centavos = 100.00 (locale-dependent decimal separator)
        String expectedAmount = String.format("BRL %.2f", 100.0);
        assertThat(captor.getValue().getTemplateVarsMap().get("amount"))
                .isEqualTo(expectedAmount);
    }

    @Test
    void shouldIncludeAuthCodeInTemplateVars() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-004")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        sender.notifyCustomer(createPayment(Payment.PaymentStatus.APPROVED));

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        assertThat(captor.getValue().getTemplateVarsMap())
                .containsEntry("auth_code", "ABC12345")
                .containsEntry("payment_id", "PAY-001");
    }

    @Test
    void shouldSetHighPriority() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-005")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        sender.notifyCustomer(createPayment(Payment.PaymentStatus.APPROVED));

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        assertThat(captor.getValue().getPriority())
                .isEqualTo(com.example.grpc.notification.NotificationPriority.HIGH);
        assertThat(captor.getValue().getChannel())
                .isEqualTo(com.example.grpc.notification.NotificationChannel.EMAIL);
    }

    @Test
    void shouldUsePendingTemplateForNonApprovedStatus() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-006")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        // PENDING não é APPROVED, então deve usar template "payment_rejected"
        sender.notifyCustomer(createPayment(Payment.PaymentStatus.PENDING));

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        assertThat(captor.getValue().getTemplateId()).isEqualTo("payment_rejected");
    }

    @Test
    void shouldFallbackWithDifferentExceptionTypes() {
        Payment payment = createPayment(Payment.PaymentStatus.APPROVED);

        // Timeout
        assertThatCode(() -> sender.notificationFallback(payment,
                new StatusRuntimeException(Status.DEADLINE_EXCEEDED)))
                .doesNotThrowAnyException();

        // Connection refused
        assertThatCode(() -> sender.notificationFallback(payment,
                new RuntimeException("Connection refused")))
                .doesNotThrowAnyException();

        // Null exception
        assertThatCode(() -> sender.notificationFallback(payment,
                new NullPointerException()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldFormatSmallAmountCorrectly() {
        when(notificationClient.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.newBuilder()
                        .setNotificationId("NOTIF-007")
                        .setStatus(NotificationStatus.SENT)
                        .build());

        Payment payment = Payment.builder()
                .paymentId("PAY-002")
                .customerId("CUST-002")
                .amount(Payment.Money.builder().currency("USD").amountCents(1L).build())
                .authorizationCode("XYZ99999")
                .status(Payment.PaymentStatus.APPROVED)
                .createdAt(java.time.Instant.now())
                .build();

        sender.notifyCustomer(payment);

        ArgumentCaptor<SendNotificationRequest> captor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        // 1 centavo = 0.01 (locale-dependent decimal separator)
        String expectedAmount = String.format("USD %.2f", 0.01);
        assertThat(captor.getValue().getTemplateVarsMap().get("amount"))
                .isEqualTo(expectedAmount);
    }
}
