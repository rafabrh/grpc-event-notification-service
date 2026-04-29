package com.example.notification.repository;

import com.example.notification.domain.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest {

    private NotificationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new NotificationRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Notification notification = Notification.builder()
                .notificationId("NOTIF-001")
                .recipientId("USER-001")
                .status(Notification.Status.SENT)
                .sentAt(Instant.now())
                .build();

        repository.save(notification);

        Optional<Notification> found = repository.findById("NOTIF-001");
        assertThat(found).isPresent();
        assertThat(found.get().getRecipientId()).isEqualTo("USER-001");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Notification> found = repository.findById("NON_EXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldOverwriteExistingNotification() {
        Notification original = Notification.builder()
                .notificationId("NOTIF-001")
                .status(Notification.Status.QUEUED)
                .build();
        repository.save(original);

        Notification updated = Notification.builder()
                .notificationId("NOTIF-001")
                .status(Notification.Status.SENT)
                .build();
        repository.save(updated);

        Optional<Notification> found = repository.findById("NOTIF-001");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Notification.Status.SENT);
    }

    @Test
    void shouldThrowOnNullId() {
        // ConcurrentHashMap não aceita chaves null
        org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.findById(null)
        );
    }
}
