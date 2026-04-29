package com.example.notification.service;

import com.example.notification.domain.Notification;
import com.example.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(repository);
    }

    @Test
    void shouldSendNotificationSuccessfully() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.sendNotification(
                "USER-001",
                Notification.Channel.EMAIL,
                Notification.Priority.HIGH,
                "payment_approved",
                Map.of("payment_id", "PAY-001"),
                "user@example.com",
                "",
                ""
        );

        assertThat(result.getNotificationId()).isNotNull();
        assertThat(result.getRecipientId()).isEqualTo("USER-001");
        assertThat(result.getChannel()).isEqualTo(Notification.Channel.EMAIL);
        assertThat(result.getPriority()).isEqualTo(Notification.Priority.HIGH);
        assertThat(result.getTemplateId()).isEqualTo("payment_approved");
        assertThat(result.getEmailAddress()).isEqualTo("user@example.com");
        assertThat(result.getSentAt()).isNotNull();
        // O status deve ser SENT ou FAILED (não-determinístico por causa do Math.random)
        assertThat(result.getStatus()).isIn(Notification.Status.SENT, Notification.Status.FAILED);
        verify(repository).save(any(Notification.class));
    }

    @Test
    void shouldSetDeliveredAtOnSuccess() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        // Roda múltiplas vezes para garantir que pelo menos uma retorna SENT
        boolean foundSent = false;
        for (int i = 0; i < 50; i++) {
            Notification result = notificationService.sendNotification(
                    "USER-001", Notification.Channel.EMAIL, Notification.Priority.LOW,
                    "template", Map.of(), "e@e.com", "", ""
            );
            if (result.getStatus() == Notification.Status.SENT) {
                assertThat(result.getDeliveredAt()).isNotNull();
                foundSent = true;
                break;
            }
        }
        assertThat(foundSent).as("Deveria encontrar pelo menos uma notificação SENT em 50 tentativas").isTrue();
    }

    @Test
    void shouldGetNotificationById() {
        String id = UUID.randomUUID().toString();
        Notification notification = Notification.builder()
                .notificationId(id)
                .status(Notification.Status.SENT)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getNotification(id);
        assertThat(result.getNotificationId()).isEqualTo(id);
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {
        when(repository.findById("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotification("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    void shouldPreserveTemplateVars() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> vars = Map.of("amount", "R$ 100.00", "name", "João");

        Notification result = notificationService.sendNotification(
                "USER-001", Notification.Channel.SMS, Notification.Priority.MEDIUM,
                "template", vars, "", "+5511999999999", ""
        );

        assertThat(result.getTemplateVars()).containsEntry("amount", "R$ 100.00");
        assertThat(result.getTemplateVars()).containsEntry("name", "João");
    }

    @Test
    void shouldSupportAllChannels() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        for (Notification.Channel channel : Notification.Channel.values()) {
            Notification result = notificationService.sendNotification(
                    "USER-001", channel, Notification.Priority.LOW,
                    "template", Map.of(), "e@e.com", "+55", "token"
            );
            assertThat(result.getChannel()).isEqualTo(channel);
        }
    }

    @Test
    void shouldSupportAllPriorities() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        for (Notification.Priority priority : Notification.Priority.values()) {
            Notification result = notificationService.sendNotification(
                    "USER-001", Notification.Channel.PUSH, priority,
                    "template", Map.of(), "", "", "device-token"
            );
            assertThat(result.getPriority()).isEqualTo(priority);
        }
    }
}
