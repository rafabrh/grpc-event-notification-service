package com.example.payment.mapper;

import com.example.grpc.common.ResponseMetadata;
import com.example.grpc.payment.*;
import com.example.payment.domain.Payment;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper entre Proto messages e Domain models.
 * Em produção: considerar MapStruct para reduzir boilerplate.
 */
@Component
public class PaymentMapper {
    
    public Payment toDomain(PaymentRequest request) {
        return Payment.builder()
                .customerId(request.getCustomerId())
                .orderId(request.getOrderId())
                .amount(toDomainMoney(request.getAmount()))
                .method(toDomainMethod(request.getMethod()))
                .metadata(request.getMetadataMap())
                .build();
    }
    
    public PaymentResponse toProto(Payment payment) {
        return PaymentResponse.newBuilder()
                .setPaymentId(payment.getPaymentId())
                .setStatus(toProtoStatus(payment.getStatus()))
                .setAuthorizationCode(payment.getAuthorizationCode())
                .setCreatedAt(toTimestamp(payment.getCreatedAt()))
                .setMetadata(buildMetadata())
                .build();
    }
    
    private Payment.Money toDomainMoney(Money protoMoney) {
        return Payment.Money.builder()
                .currency(protoMoney.getCurrency())
                .amountCents(protoMoney.getAmountCents())
                .build();
    }
    
    private Payment.PaymentMethod toDomainMethod(PaymentMethod protoMethod) {
        return switch (protoMethod) {
            case CREDIT_CARD -> Payment.PaymentMethod.CREDIT_CARD;
            case DEBIT_CARD -> Payment.PaymentMethod.DEBIT_CARD;
            case PIX -> Payment.PaymentMethod.PIX;
            case BOLETO -> Payment.PaymentMethod.BOLETO;
            default -> throw new IllegalArgumentException("Unknown payment method: " + protoMethod);
        };
    }
    
    private PaymentStatus toProtoStatus(Payment.PaymentStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> PaymentStatus.PENDING;
            case PROCESSING -> PaymentStatus.PROCESSING;
            case APPROVED -> PaymentStatus.APPROVED;
            case REJECTED -> PaymentStatus.REJECTED;
            case CANCELLED -> PaymentStatus.CANCELLED;
        };
    }
    
    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
    
    private ResponseMetadata buildMetadata() {
        return ResponseMetadata.newBuilder()
                .setRequestId(java.util.UUID.randomUUID().toString())
                .setTimestamp(toTimestamp(Instant.now()))
                .setServiceVersion("1.0.0")
                .build();
    }
}
