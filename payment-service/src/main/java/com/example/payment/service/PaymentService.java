package com.example.payment.service;

import com.example.payment.domain.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer com lógica de negócio.
 *
 * Patterns implementados:
 * - Circuit Breaker para chamadas ao Notification Service (via PaymentNotificationSender)
 * - Retry com backoff
 * - Fallback graceful
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentNotificationSender notificationSender;

    public Payment processPayment(String customerId, String orderId,
                                   Payment.Money amount, Payment.PaymentMethod method,
                                   Map<String, String> metadata) {

        log.info("Processing payment: customerId={}, orderId={}, amount={}",
                customerId, orderId, amount.getAmountCents());

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .customerId(customerId)
                .orderId(orderId)
                .amount(amount)
                .method(method)
                .status(simulatePaymentProcessing(amount))
                .authorizationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .metadata(metadata)
                .build();

        repository.save(payment);

        // Notificar cliente via componente separado (AOP funciona corretamente)
        notificationSender.notifyCustomer(payment);

        return payment;
    }

    public Payment getPayment(String paymentId) {
        return repository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }

    public List<Payment> getPaymentsByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Payment cancelPayment(String paymentId, String reason) {
        Payment payment = getPayment(paymentId);

        if (payment.getStatus() != Payment.PaymentStatus.PENDING
                && payment.getStatus() != Payment.PaymentStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot cancel payment in status " + payment.getStatus()
                    + ". Only PENDING and PROCESSING payments can be cancelled.");
        }

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setUpdatedAt(Instant.now());

        repository.save(payment);

        log.info("Payment cancelled: paymentId={}, reason={}", paymentId, reason);

        return payment;
    }

    /**
     * Simula aprovação/rejeição baseado no valor.
     * Em produção: integração com gateway real.
     */
    private Payment.PaymentStatus simulatePaymentProcessing(Payment.Money amount) {
        if (amount.getAmountCents() > 1_000_000) {
            return Payment.PaymentStatus.REJECTED;
        }
        return Payment.PaymentStatus.APPROVED;
    }
}
