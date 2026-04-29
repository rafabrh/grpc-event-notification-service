package com.example.payment.service;

import com.example.payment.domain.Payment;
import com.example.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private PaymentNotificationSender notificationSender;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(repository, notificationSender);
    }

    @Test
    void shouldProcessPaymentAndReturnApprovedForSmallAmount() {
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment.Money amount = Payment.Money.builder()
                .currency("BRL")
                .amountCents(10000L)
                .build();

        Payment result = paymentService.processPayment(
                "CUST001", "ORD001", amount, Payment.PaymentMethod.PIX, Map.of()
        );

        assertThat(result.getPaymentId()).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo("CUST001");
        assertThat(result.getOrderId()).isEqualTo("ORD001");
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.APPROVED);
        assertThat(result.getAuthorizationCode()).isNotNull().hasSize(8);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(repository).save(any(Payment.class));
        verify(notificationSender).notifyCustomer(any(Payment.class));
    }

    @Test
    void shouldRejectPaymentAbove10000BRL() {
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment.Money amount = Payment.Money.builder()
                .currency("BRL")
                .amountCents(1_000_001L)
                .build();

        Payment result = paymentService.processPayment(
                "CUST001", "ORD001", amount, Payment.PaymentMethod.CREDIT_CARD, Map.of()
        );

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.REJECTED);
    }

    @Test
    void shouldApprovePaymentAtExactly10000BRL() {
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment.Money amount = Payment.Money.builder()
                .currency("BRL")
                .amountCents(1_000_000L)
                .build();

        Payment result = paymentService.processPayment(
                "CUST001", "ORD001", amount, Payment.PaymentMethod.PIX, Map.of()
        );

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.APPROVED);
    }

    @Test
    void shouldGetPaymentById() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.APPROVED)
                .build();

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPayment(paymentId);

        assertThat(result.getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        when(repository.findById("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void shouldGetPaymentsByCustomer() {
        Payment p1 = Payment.builder().paymentId("PAY-1").customerId("CUST001").build();
        Payment p2 = Payment.builder().paymentId("PAY-2").customerId("CUST001").build();

        when(repository.findByCustomerId("CUST001")).thenReturn(List.of(p1, p2));

        List<Payment> result = paymentService.getPaymentsByCustomer("CUST001");

        assertThat(result).hasSize(2);
        verify(repository).findByCustomerId("CUST001");
    }

    @Test
    void shouldCancelPendingPayment() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.cancelPayment(paymentId, "User requested");

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.CANCELLED);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCancelProcessingPayment() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.cancelPayment(paymentId, "Timeout");

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.CANCELLED);
    }

    @Test
    void shouldNotCancelApprovedPayment() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.APPROVED)
                .build();

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, "Too late"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel payment in status APPROVED");
    }

    @Test
    void shouldNotCancelRejectedPayment() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.REJECTED)
                .build();

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel payment in status REJECTED");
    }

    @Test
    void shouldNotCancelAlreadyCancelledPayment() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.CANCELLED)
                .build();

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel payment in status CANCELLED");
    }

    @Test
    void shouldThrowWhenCancellingNonExistentPayment() {
        when(repository.findById("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.cancelPayment("INVALID", "reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void shouldPreserveMetadata() {
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> metadata = Map.of("ip", "10.0.0.1", "device", "mobile");
        Payment.Money amount = Payment.Money.builder()
                .currency("BRL")
                .amountCents(5000L)
                .build();

        Payment result = paymentService.processPayment(
                "CUST001", "ORD001", amount, Payment.PaymentMethod.PIX, metadata
        );

        assertThat(result.getMetadata()).containsEntry("ip", "10.0.0.1");
        assertThat(result.getMetadata()).containsEntry("device", "mobile");
    }

    @Test
    void shouldSetPaymentMethodCorrectly() {
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment.Money amount = Payment.Money.builder()
                .currency("BRL")
                .amountCents(5000L)
                .build();

        for (Payment.PaymentMethod method : Payment.PaymentMethod.values()) {
            Payment result = paymentService.processPayment(
                    "CUST001", "ORD001", amount, method, Map.of()
            );
            assertThat(result.getMethod()).isEqualTo(method);
        }
    }
}
