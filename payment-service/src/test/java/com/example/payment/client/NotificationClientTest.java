package com.example.payment.client;

import com.example.grpc.notification.NotificationResponse;
import com.example.grpc.notification.NotificationServiceGrpc;
import com.example.grpc.notification.NotificationStatus;
import com.example.grpc.notification.SendNotificationRequest;
import com.example.grpc.notification.NotificationChannel;
import com.example.grpc.notification.NotificationPriority;
import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes do NotificationClient com servidor gRPC in-process.
 * Simula cenários reais de comunicação entre serviços.
 */
class NotificationClientTest {

    private static final String SERVER_NAME = "notification-test-" + UUID.randomUUID();

    private Server server;
    private ManagedChannel channel;
    private NotificationClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new NotificationClient();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (channel != null) {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (server != null) {
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void shouldSendNotificationSuccessfully() throws Exception {
        String expectedNotifId = UUID.randomUUID().toString();

        startServer(new NotificationServiceGrpc.NotificationServiceImplBase() {
            @Override
            public void sendNotification(SendNotificationRequest request,
                                          StreamObserver<NotificationResponse> responseObserver) {
                responseObserver.onNext(NotificationResponse.newBuilder()
                        .setNotificationId(expectedNotifId)
                        .setStatus(NotificationStatus.SENT)
                        .build());
                responseObserver.onCompleted();
            }
        });

        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.EMAIL)
                .setPriority(NotificationPriority.HIGH)
                .setTemplateId("payment_approved")
                .setEmailAddress("user@example.com")
                .build();

        NotificationResponse response = client.sendNotification(request);

        assertThat(response.getNotificationId()).isEqualTo(expectedNotifId);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void shouldPropagateStatusRuntimeExceptionOnFailure() throws Exception {
        startServer(new NotificationServiceGrpc.NotificationServiceImplBase() {
            @Override
            public void sendNotification(SendNotificationRequest request,
                                          StreamObserver<NotificationResponse> responseObserver) {
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription("Service is down")
                        .asRuntimeException());
            }
        });

        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.EMAIL)
                .setTemplateId("template")
                .build();

        assertThatThrownBy(() -> client.sendNotification(request))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> {
                    StatusRuntimeException sre = (StatusRuntimeException) e;
                    assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
                });
    }

    @Test
    void shouldTimeoutWithDeadlineExceeded() throws Exception {
        startServer(new NotificationServiceGrpc.NotificationServiceImplBase() {
            @Override
            public void sendNotification(SendNotificationRequest request,
                                          StreamObserver<NotificationResponse> responseObserver) {
                // Simula resposta lenta — nunca responde
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.EMAIL)
                .setTemplateId("template")
                .build();

        assertThatThrownBy(() -> client.sendNotification(request))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> {
                    StatusRuntimeException sre = (StatusRuntimeException) e;
                    assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.DEADLINE_EXCEEDED);
                });
    }

    @Test
    void shouldHandleInternalServerError() throws Exception {
        startServer(new NotificationServiceGrpc.NotificationServiceImplBase() {
            @Override
            public void sendNotification(SendNotificationRequest request,
                                          StreamObserver<NotificationResponse> responseObserver) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Database crash")
                        .asRuntimeException());
            }
        });

        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setRecipientId("USER-001")
                .setChannel(NotificationChannel.SMS)
                .setTemplateId("template")
                .build();

        assertThatThrownBy(() -> client.sendNotification(request))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> {
                    StatusRuntimeException sre = (StatusRuntimeException) e;
                    assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                });
    }

    private void startServer(BindableService service) throws Exception {
        server = InProcessServerBuilder.forName(SERVER_NAME)
                .directExecutor()
                .addService(service)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(SERVER_NAME)
                .directExecutor()
                .build();

        // Inject the stub via reflection (simula o que @GrpcClient faz)
        NotificationServiceGrpc.NotificationServiceBlockingStub stub =
                NotificationServiceGrpc.newBlockingStub(channel);

        Field stubField = NotificationClient.class.getDeclaredField("notificationStub");
        stubField.setAccessible(true);
        stubField.set(client, stub);
    }
}
