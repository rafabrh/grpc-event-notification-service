package com.example.notification.grpc;

import com.example.grpc.notification.*;
import com.example.notification.domain.Notification;
import com.example.notification.service.NotificationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationGrpcServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private StreamObserver<NotificationResponse> responseObserver;

    private NotificationGrpcService grpcService;

    @BeforeEach
    void setUp() {
        grpcService = new NotificationGrpcService(notificationService);
    }

    @Test
    void shouldSendNotificationSuccessfully() {
        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.EMAIL)
                .setPriority(NotificationPriority.HIGH)
                .setTemplateId("payment_approved")
                .setEmailAddress("user@example.com")
                .putTemplateVars("payment_id", "PAY-001")
                .build();

        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .recipientId("USER-001")
                .channel(Notification.Channel.EMAIL)
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        when(notificationService.sendNotification(
                eq("USER-001"),
                eq(Notification.Channel.EMAIL),
                eq(Notification.Priority.HIGH),
                eq("payment_approved"),
                anyMap(),
                eq("user@example.com"),
                anyString(),
                anyString()
        )).thenReturn(notification);

        grpcService.sendNotification(request, responseObserver);

        verify(responseObserver).onNext(any(NotificationResponse.class));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void shouldRejectEmptyRecipientId() {
        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("")
                .setChannel(NotificationChannel.EMAIL)
                .setTemplateId("template")
                .build();

        grpcService.sendNotification(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .contains("recipient_id is required");
    }

    @Test
    void shouldRejectUnspecifiedChannel() {
        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.NOTIFICATION_CHANNEL_UNSPECIFIED)
                .setTemplateId("template")
                .build();

        grpcService.sendNotification(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .contains("channel is required");
    }

    @Test
    void shouldRejectEmptyTemplateId() {
        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.EMAIL)
                .setTemplateId("")
                .build();

        grpcService.sendNotification(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .contains("template_id is required");
    }

    @Test
    void shouldHandleInternalError() {
        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.EMAIL)
                .setPriority(NotificationPriority.HIGH)
                .setTemplateId("template")
                .build();

        when(notificationService.sendNotification(any(), any(), any(), any(), anyMap(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        grpcService.sendNotification(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INTERNAL);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .doesNotContain("Database");
    }

    @Test
    void shouldGetNotificationById() {
        String notificationId = UUID.randomUUID().toString();
        GetNotificationRequest request = GetNotificationRequest.newBuilder()
                .setNotificationId(notificationId)
                .build();

        Notification notification = Notification.builder()
                .notificationId(notificationId)
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        when(notificationService.getNotification(notificationId)).thenReturn(notification);

        grpcService.getNotification(request, responseObserver);

        verify(responseObserver).onNext(any(NotificationResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void shouldReturnNotFoundForInvalidNotificationId() {
        GetNotificationRequest request = GetNotificationRequest.newBuilder()
                .setNotificationId("INVALID")
                .build();

        when(notificationService.getNotification("INVALID"))
                .thenThrow(new IllegalArgumentException("Notification not found: INVALID"));

        grpcService.getNotification(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void shouldHandleBatchSendStream() {
        @SuppressWarnings("unchecked")
        StreamObserver<BatchSendResponse> batchObserver = mock(StreamObserver.class);

        Notification sentNotification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        when(notificationService.sendNotification(any(), any(), any(), any(), anyMap(), any(), any(), any()))
                .thenReturn(sentNotification);

        StreamObserver<SendNotificationRequest> requestObserver = grpcService.batchSend(batchObserver);

        // Envia 3 notificações
        for (int i = 0; i < 3; i++) {
            requestObserver.onNext(SendNotificationRequest.newBuilder()
                    .setRecipientId("USER-" + i)
                    .setChannel(NotificationChannel.EMAIL)
                    .setPriority(NotificationPriority.MEDIUM)
                    .setTemplateId("template")
                    .build());
        }
        requestObserver.onCompleted();

        ArgumentCaptor<BatchSendResponse> captor = ArgumentCaptor.forClass(BatchSendResponse.class);
        verify(batchObserver).onNext(captor.capture());
        verify(batchObserver).onCompleted();

        BatchSendResponse response = captor.getValue();
        assertThat(response.getTotalSent()).isEqualTo(3);
        assertThat(response.getTotalFailed()).isEqualTo(0);
        assertThat(response.getResultsList()).hasSize(3);
    }

    @Test
    void shouldCountFailuresInBatchSend() {
        @SuppressWarnings("unchecked")
        StreamObserver<BatchSendResponse> batchObserver = mock(StreamObserver.class);

        when(notificationService.sendNotification(any(), any(), any(), any(), anyMap(), any(), any(), any()))
                .thenThrow(new RuntimeException("Provider down"));

        StreamObserver<SendNotificationRequest> requestObserver = grpcService.batchSend(batchObserver);

        requestObserver.onNext(SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.SMS)
                .setPriority(NotificationPriority.HIGH)
                .setTemplateId("template")
                .build());
        requestObserver.onCompleted();

        ArgumentCaptor<BatchSendResponse> captor = ArgumentCaptor.forClass(BatchSendResponse.class);
        verify(batchObserver).onNext(captor.capture());

        assertThat(captor.getValue().getTotalSent()).isEqualTo(0);
        assertThat(captor.getValue().getTotalFailed()).isEqualTo(1);
    }

    @Test
    void shouldMapAllChannels() {
        NotificationChannel[] channels = {
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.PUSH,
                NotificationChannel.WHATSAPP
        };

        for (NotificationChannel channel : channels) {
            Notification notification = Notification.builder()
                    .notificationId(UUID.randomUUID().toString())
                    .status(Notification.Status.SENT)
                    .sentAt(Instant.now())
                    .build();

            when(notificationService.sendNotification(any(), any(), any(), any(), anyMap(), any(), any(), any()))
                    .thenReturn(notification);

            SendNotificationRequest request = SendNotificationRequest.newBuilder()
                    .setRecipientId("USER-001")
                    .setChannel(channel)
                    .setPriority(NotificationPriority.MEDIUM)
                    .setTemplateId("template")
                    .build();

            grpcService.sendNotification(request, responseObserver);
        }

        verify(responseObserver, times(4)).onCompleted();
    }

    // === EDGE CASES — Cenários de produção ===

    @Test
    void shouldHandleInternalErrorOnGetNotification() {
        GetNotificationRequest request = GetNotificationRequest.newBuilder()
                .setNotificationId("NOTIF-001")
                .build();

        when(notificationService.getNotification("NOTIF-001"))
                .thenThrow(new RuntimeException("Repository crash"));

        grpcService.getNotification(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INTERNAL);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .doesNotContain("Repository");
    }

    @Test
    void shouldHandleMonitorNotificationsBidirectionalStream() {
        @SuppressWarnings("unchecked")
        StreamObserver<NotificationResponse> monitorObserver = mock(StreamObserver.class);

        Notification notification = Notification.builder()
                .notificationId("NOTIF-001")
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        when(notificationService.getNotification("NOTIF-001")).thenReturn(notification);

        StreamObserver<GetNotificationRequest> requestObserver =
                grpcService.monitorNotifications(monitorObserver);

        // Cliente envia request de monitor
        requestObserver.onNext(GetNotificationRequest.newBuilder()
                .setNotificationId("NOTIF-001")
                .build());

        // Verifica que servidor respondeu
        verify(monitorObserver).onNext(any(NotificationResponse.class));

        // Cliente fecha stream
        requestObserver.onCompleted();
        verify(monitorObserver).onCompleted();
    }

    @Test
    void shouldHandleMonitorWithNonExistentNotification() {
        @SuppressWarnings("unchecked")
        StreamObserver<NotificationResponse> monitorObserver = mock(StreamObserver.class);

        when(notificationService.getNotification("INVALID"))
                .thenThrow(new IllegalArgumentException("Not found"));

        StreamObserver<GetNotificationRequest> requestObserver =
                grpcService.monitorNotifications(monitorObserver);

        requestObserver.onNext(GetNotificationRequest.newBuilder()
                .setNotificationId("INVALID")
                .build());

        // Não deve enviar erro no stream, apenas logar warning
        verify(monitorObserver, never()).onNext(any());
        verify(monitorObserver, never()).onError(any());

        requestObserver.onCompleted();
        verify(monitorObserver).onCompleted();
    }

    @Test
    void shouldHandleMultipleMonitorRequests() {
        @SuppressWarnings("unchecked")
        StreamObserver<NotificationResponse> monitorObserver = mock(StreamObserver.class);

        Notification n1 = Notification.builder()
                .notificationId("NOTIF-001")
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        Notification n2 = Notification.builder()
                .notificationId("NOTIF-002")
                .status(Notification.Status.DELIVERED)
                .sentAt(Instant.now())
                .build();

        when(notificationService.getNotification("NOTIF-001")).thenReturn(n1);
        when(notificationService.getNotification("NOTIF-002")).thenReturn(n2);

        StreamObserver<GetNotificationRequest> requestObserver =
                grpcService.monitorNotifications(monitorObserver);

        requestObserver.onNext(GetNotificationRequest.newBuilder()
                .setNotificationId("NOTIF-001").build());
        requestObserver.onNext(GetNotificationRequest.newBuilder()
                .setNotificationId("NOTIF-002").build());
        requestObserver.onCompleted();

        verify(monitorObserver, times(2)).onNext(any());
        verify(monitorObserver).onCompleted();
    }

    @Test
    void shouldHandleBatchSendWithMixOfSuccessAndFailure() {
        @SuppressWarnings("unchecked")
        StreamObserver<BatchSendResponse> batchObserver = mock(StreamObserver.class);

        Notification sentNotification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        Notification failedNotification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .status(Notification.Status.FAILED)
                .sentAt(Instant.now())
                .build();

        when(notificationService.sendNotification(any(), any(), any(), any(), anyMap(), any(), any(), any()))
                .thenReturn(sentNotification)
                .thenReturn(failedNotification)
                .thenReturn(sentNotification);

        StreamObserver<SendNotificationRequest> requestObserver = grpcService.batchSend(batchObserver);

        for (int i = 0; i < 3; i++) {
            requestObserver.onNext(SendNotificationRequest.newBuilder()
                    .setRecipientId("USER-" + i)
                    .setChannel(NotificationChannel.EMAIL)
                    .setPriority(NotificationPriority.MEDIUM)
                    .setTemplateId("template")
                    .build());
        }
        requestObserver.onCompleted();

        ArgumentCaptor<BatchSendResponse> captor = ArgumentCaptor.forClass(BatchSendResponse.class);
        verify(batchObserver).onNext(captor.capture());

        BatchSendResponse response = captor.getValue();
        assertThat(response.getTotalSent()).isEqualTo(2);
        assertThat(response.getTotalFailed()).isEqualTo(1);
        assertThat(response.getResultsList()).hasSize(3);
    }

    @Test
    void shouldHandleEmptyBatchSend() {
        @SuppressWarnings("unchecked")
        StreamObserver<BatchSendResponse> batchObserver = mock(StreamObserver.class);

        StreamObserver<SendNotificationRequest> requestObserver = grpcService.batchSend(batchObserver);
        requestObserver.onCompleted();

        ArgumentCaptor<BatchSendResponse> captor = ArgumentCaptor.forClass(BatchSendResponse.class);
        verify(batchObserver).onNext(captor.capture());

        BatchSendResponse response = captor.getValue();
        assertThat(response.getTotalSent()).isEqualTo(0);
        assertThat(response.getTotalFailed()).isEqualTo(0);
        assertThat(response.getResultsList()).isEmpty();
    }

    @Test
    void shouldMapAllPriorities() {
        NotificationPriority[] priorities = {
                NotificationPriority.LOW,
                NotificationPriority.MEDIUM,
                NotificationPriority.HIGH,
                NotificationPriority.URGENT
        };

        for (NotificationPriority priority : priorities) {
            @SuppressWarnings("unchecked")
            StreamObserver<NotificationResponse> observer = mock(StreamObserver.class);

            Notification notification = Notification.builder()
                    .notificationId(UUID.randomUUID().toString())
                    .status(Notification.Status.SENT)
                    .sentAt(Instant.now())
                    .build();

            when(notificationService.sendNotification(any(), any(), any(), any(), anyMap(), any(), any(), any()))
                    .thenReturn(notification);

            SendNotificationRequest request = SendNotificationRequest.newBuilder()
                    .setRecipientId("USER-001")
                    .setChannel(NotificationChannel.EMAIL)
                    .setPriority(priority)
                    .setTemplateId("template")
                    .build();

            grpcService.sendNotification(request, observer);
            verify(observer).onCompleted();
        }
    }
}
