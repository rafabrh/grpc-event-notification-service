package com.example.payment.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Domain model do Payment.
 * Em produção real, isso seria uma @Entity JPA.
 */
@Data
@Builder
public class Payment {
    
    private String paymentId;
    private String customerId;
    private String orderId;
    private Money amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String authorizationCode;
    private Instant createdAt;
    private Instant updatedAt;
    private Map<String, String> metadata;
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PIX, BOLETO
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, APPROVED, REJECTED, CANCELLED
    }
    
    @Data
    @Builder
    public static class Money {
        private String currency;
        private Long amountCents;
    }
}
