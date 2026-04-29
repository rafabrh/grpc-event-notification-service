package com.example.payment.repository;

import com.example.payment.domain.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRepositoryTest {

    private PaymentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PaymentRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Payment payment = Payment.builder()
                .paymentId("PAY-001")
                .customerId("CUST-001")
                .status(Payment.PaymentStatus.APPROVED)
                .createdAt(Instant.now())
                .build();

        repository.save(payment);

        Optional<Payment> found = repository.findById("PAY-001");
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("CUST-001");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Payment> found = repository.findById("NON_EXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAll() {
        Payment p1 = Payment.builder().paymentId("PAY-001").status(Payment.PaymentStatus.APPROVED).build();
        Payment p2 = Payment.builder().paymentId("PAY-002").status(Payment.PaymentStatus.PENDING).build();

        repository.save(p1);
        repository.save(p2);

        Map<String, Payment> all = repository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).containsKeys("PAY-001", "PAY-002");
    }

    @Test
    void shouldOverwriteExistingPayment() {
        Payment original = Payment.builder()
                .paymentId("PAY-001")
                .status(Payment.PaymentStatus.PENDING)
                .build();
        repository.save(original);

        Payment updated = Payment.builder()
                .paymentId("PAY-001")
                .status(Payment.PaymentStatus.APPROVED)
                .build();
        repository.save(updated);

        Optional<Payment> found = repository.findById("PAY-001");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Payment.PaymentStatus.APPROVED);
    }

    @Test
    void shouldFindByCustomerId() {
        Payment p1 = Payment.builder().paymentId("PAY-001").customerId("CUST-001").build();
        Payment p2 = Payment.builder().paymentId("PAY-002").customerId("CUST-001").build();
        Payment p3 = Payment.builder().paymentId("PAY-003").customerId("CUST-002").build();

        repository.save(p1);
        repository.save(p2);
        repository.save(p3);

        List<Payment> result = repository.findByCustomerId("CUST-001");
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Payment::getPaymentId).containsExactlyInAnyOrder("PAY-001", "PAY-002");
    }

    @Test
    void shouldReturnEmptyListForUnknownCustomer() {
        List<Payment> result = repository.findByCustomerId("UNKNOWN");
        assertThat(result).isEmpty();
    }

    @Test
    void findAllShouldReturnImmutableCopy() {
        Payment p1 = Payment.builder().paymentId("PAY-001").build();
        repository.save(p1);

        Map<String, Payment> all = repository.findAll();

        // A cópia retornada não deve afetar o estado interno
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> all.put("PAY-002", Payment.builder().paymentId("PAY-002").build())
        );
    }
}
