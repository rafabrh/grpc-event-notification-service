package com.example.payment.mapper;

import com.example.grpc.payment.*;
import com.example.payment.domain.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentMapperTest {

    private PaymentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentMapper();
    }

    @Test
    void shouldMapDomainToProto() {
        Instant now = Instant.now();
        Payment payment = Payment.builder()
                .paymentId("PAY-001")
                .customerId("CUST-001")
                .orderId("ORD-001")
                .amount(Payment.Money.builder().currency("BRL").amountCents(10000L).build())
                .method(Payment.PaymentMethod.PIX)
                .status(Payment.PaymentStatus.APPROVED)
                .authorizationCode("ABC12345")
                .createdAt(now)
                .updatedAt(now)
                .metadata(Map.of("key", "value"))
                .build();

        PaymentResponse response = mapper.toProto(payment);

        assertThat(response.getPaymentId()).isEqualTo("PAY-001");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getAuthorizationCode()).isEqualTo("ABC12345");
        assertThat(response.getCreatedAt().getSeconds()).isEqualTo(now.getEpochSecond());
        assertThat(response.getCreatedAt().getNanos()).isEqualTo(now.getNano());
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().getRequestId()).isNotBlank();
        assertThat(response.getMetadata().getServiceVersion()).isEqualTo("1.0.0");
    }

    @Test
    void shouldMapProtoToDomainy() {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("CUST-001")
                .setOrderId("ORD-001")
                .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(5000).build())
                .setMethod(PaymentMethod.CREDIT_CARD)
                .putMetadata("ip", "10.0.0.1")
                .build();

        Payment payment = mapper.toDomain(request);

        assertThat(payment.getCustomerId()).isEqualTo("CUST-001");
        assertThat(payment.getOrderId()).isEqualTo("ORD-001");
        assertThat(payment.getAmount().getCurrency()).isEqualTo("BRL");
        assertThat(payment.getAmount().getAmountCents()).isEqualTo(5000L);
        assertThat(payment.getMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        assertThat(payment.getMetadata()).containsEntry("ip", "10.0.0.1");
    }

    @Test
    void shouldMapAllPaymentStatuses() {
        Instant now = Instant.now();
        for (Payment.PaymentStatus status : Payment.PaymentStatus.values()) {
            Payment payment = Payment.builder()
                    .paymentId("PAY-001")
                    .status(status)
                    .authorizationCode("ABC")
                    .createdAt(now)
                    .build();

            PaymentResponse response = mapper.toProto(payment);
            assertThat(response.getStatus().name()).isEqualTo(status.name());
        }
    }

    @Test
    void shouldMapAllPaymentMethods() {
        PaymentMethod[] protoMethods = {
                PaymentMethod.CREDIT_CARD,
                PaymentMethod.DEBIT_CARD,
                PaymentMethod.PIX,
                PaymentMethod.BOLETO
        };
        Payment.PaymentMethod[] domainMethods = {
                Payment.PaymentMethod.CREDIT_CARD,
                Payment.PaymentMethod.DEBIT_CARD,
                Payment.PaymentMethod.PIX,
                Payment.PaymentMethod.BOLETO
        };

        for (int i = 0; i < protoMethods.length; i++) {
            PaymentRequest request = PaymentRequest.newBuilder()
                    .setCustomerId("C")
                    .setOrderId("O")
                    .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(100).build())
                    .setMethod(protoMethods[i])
                    .build();

            Payment payment = mapper.toDomain(request);
            assertThat(payment.getMethod()).isEqualTo(domainMethods[i]);
        }
    }

    @Test
    void shouldThrowForUnknownPaymentMethod() {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("C")
                .setOrderId("O")
                .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(100).build())
                .setMethod(PaymentMethod.PAYMENT_METHOD_UNSPECIFIED)
                .build();

        assertThatThrownBy(() -> mapper.toDomain(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
