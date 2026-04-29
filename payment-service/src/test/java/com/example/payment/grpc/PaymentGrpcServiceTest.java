package com.example.payment.grpc;

import com.example.grpc.payment.*;
import com.example.payment.domain.Payment;
import com.example.payment.mapper.PaymentMapper;
import com.example.payment.service.PaymentService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do PaymentGrpcService.
 * 
 * Patterns testados:
 * - Validação de input
 * - Mapeamento proto <-> domain
 * - Error handling com status codes
 * - Integração com service layer
 */
@ExtendWith(MockitoExtension.class)
class PaymentGrpcServiceTest {
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private PaymentMapper mapper;
    
    @Mock
    private StreamObserver<PaymentResponse> responseObserver;
    
    private PaymentGrpcService grpcService;
    
    @BeforeEach
    void setUp() {
        grpcService = new PaymentGrpcService(paymentService, mapper);
    }
    
    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("CUST001")
                .setOrderId("ORD123")
                .setAmount(Money.newBuilder()
                        .setCurrency("BRL")
                        .setAmountCents(10000)
                        .build())
                .setMethod(PaymentMethod.PIX)
                .putMetadata("ip", "192.168.1.1")
                .build();
        
        Payment domainPayment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .customerId("CUST001")
                .status(Payment.PaymentStatus.APPROVED)
                .createdAt(Instant.now())
                .build();
        
        PaymentResponse expectedResponse = PaymentResponse.newBuilder()
                .setPaymentId(domainPayment.getPaymentId())
                .setStatus(PaymentStatus.APPROVED)
                .build();
        
        when(paymentService.processPayment(
                eq("CUST001"),
                eq("ORD123"),
                any(Payment.Money.class),
                eq(Payment.PaymentMethod.PIX),
                anyMap()
        )).thenReturn(domainPayment);
        
        when(mapper.toProto(domainPayment)).thenReturn(expectedResponse);
        
        // When
        grpcService.processPayment(request, responseObserver);
        
        // Then
        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }
    
    @Test
    void shouldRejectEmptyCustomerId() {
        // Given
        PaymentRequest invalidRequest = PaymentRequest.newBuilder()
                .setCustomerId("")  // INVÁLIDO
                .setOrderId("ORD123")
                .setAmount(Money.newBuilder()
                        .setCurrency("BRL")
                        .setAmountCents(10000)
                        .build())
                .setMethod(PaymentMethod.PIX)
                .build();
        
        // When
        grpcService.processPayment(invalidRequest, responseObserver);
        
        // Then
        ArgumentCaptor<StatusRuntimeException> errorCaptor = 
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        
        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(error.getStatus().getDescription()).contains("customer_id is required");
        
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
    
    @Test
    void shouldRejectZeroAmount() {
        // Given
        PaymentRequest invalidRequest = PaymentRequest.newBuilder()
                .setCustomerId("CUST001")
                .setOrderId("ORD123")
                .setAmount(Money.newBuilder()
                        .setCurrency("BRL")
                        .setAmountCents(0)  // INVÁLIDO
                        .build())
                .setMethod(PaymentMethod.PIX)
                .build();
        
        // When
        grpcService.processPayment(invalidRequest, responseObserver);
        
        // Then
        ArgumentCaptor<StatusRuntimeException> errorCaptor = 
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        
        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(error.getStatus().getDescription()).contains("amount must be greater than zero");
    }
    
    @Test
    void shouldHandleServiceLayerException() {
        // Given
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("CUST001")
                .setOrderId("ORD123")
                .setAmount(Money.newBuilder()
                        .setCurrency("BRL")
                        .setAmountCents(10000)
                        .build())
                .setMethod(PaymentMethod.PIX)
                .build();
        
        when(paymentService.processPayment(any(), any(), any(), any(), anyMap()))
                .thenThrow(new RuntimeException("Database connection failed"));
        
        // When
        grpcService.processPayment(request, responseObserver);
        
        // Then
        ArgumentCaptor<StatusRuntimeException> errorCaptor = 
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        
        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
        assertThat(error.getStatus().getDescription()).contains("Internal server error");
        
        // Não deve expor detalhes internos
        assertThat(error.getStatus().getDescription()).doesNotContain("Database");
    }
    
    @Test
    void shouldGetPaymentById() {
        // Given
        String paymentId = UUID.randomUUID().toString();
        GetPaymentRequest request = GetPaymentRequest.newBuilder()
                .setPaymentId(paymentId)
                .build();
        
        Payment domainPayment = Payment.builder()
                .paymentId(paymentId)
                .status(Payment.PaymentStatus.APPROVED)
                .build();
        
        PaymentResponse expectedResponse = PaymentResponse.newBuilder()
                .setPaymentId(paymentId)
                .setStatus(PaymentStatus.APPROVED)
                .build();
        
        when(paymentService.getPayment(paymentId)).thenReturn(domainPayment);
        when(mapper.toProto(domainPayment)).thenReturn(expectedResponse);
        
        // When
        grpcService.getPayment(request, responseObserver);
        
        // Then
        verify(responseObserver).onNext(expectedResponse);
        verify(responseObserver).onCompleted();
    }
    
    @Test
    void shouldReturnNotFoundForInvalidPaymentId() {
        // Given
        GetPaymentRequest request = GetPaymentRequest.newBuilder()
                .setPaymentId("INVALID_ID")
                .build();
        
        when(paymentService.getPayment("INVALID_ID"))
                .thenThrow(new IllegalArgumentException("Payment not found: INVALID_ID"));
        
        // When
        grpcService.getPayment(request, responseObserver);
        
        // Then
        ArgumentCaptor<StatusRuntimeException> errorCaptor = 
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        
        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void shouldListPaymentsForCustomer() {
        Payment p1 = Payment.builder().paymentId("PAY-1").customerId("CUST001")
                .status(Payment.PaymentStatus.APPROVED).build();
        Payment p2 = Payment.builder().paymentId("PAY-2").customerId("CUST001")
                .status(Payment.PaymentStatus.PENDING).build();

        PaymentResponse resp1 = PaymentResponse.newBuilder().setPaymentId("PAY-1").build();
        PaymentResponse resp2 = PaymentResponse.newBuilder().setPaymentId("PAY-2").build();

        when(paymentService.getPaymentsByCustomer("CUST001")).thenReturn(List.of(p1, p2));
        when(mapper.toProto(p1)).thenReturn(resp1);
        when(mapper.toProto(p2)).thenReturn(resp2);

        ListPaymentsRequest request = ListPaymentsRequest.newBuilder()
                .setCustomerId("CUST001")
                .build();

        grpcService.listPayments(request, responseObserver);

        verify(responseObserver, times(2)).onNext(any());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void shouldReturnEmptyStreamForCustomerWithNoPayments() {
        when(paymentService.getPaymentsByCustomer("CUST-EMPTY")).thenReturn(List.of());

        ListPaymentsRequest request = ListPaymentsRequest.newBuilder()
                .setCustomerId("CUST-EMPTY")
                .build();

        grpcService.listPayments(request, responseObserver);

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver).onCompleted();
    }

    @Test
    void shouldRejectListPaymentsWithEmptyCustomerId() {
        ListPaymentsRequest request = ListPaymentsRequest.newBuilder()
                .setCustomerId("")
                .build();

        grpcService.listPayments(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldCancelPaymentSuccessfully() {
        @SuppressWarnings("unchecked")
        StreamObserver<CancelPaymentResponse> cancelObserver = mock(StreamObserver.class);

        Payment payment = Payment.builder()
                .paymentId("PAY-001")
                .status(Payment.PaymentStatus.CANCELLED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .authorizationCode("ABC")
                .build();

        PaymentResponse protoPayment = PaymentResponse.newBuilder()
                .setPaymentId("PAY-001")
                .setStatus(PaymentStatus.CANCELLED)
                .build();

        when(paymentService.cancelPayment("PAY-001", "user request")).thenReturn(payment);
        when(mapper.toProto(payment)).thenReturn(protoPayment);

        CancelPaymentRequest request = CancelPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .setReason("user request")
                .build();

        grpcService.cancelPayment(request, cancelObserver);

        verify(cancelObserver).onNext(any(CancelPaymentResponse.class));
        verify(cancelObserver).onCompleted();
    }

    @Test
    void shouldReturnFailedPreconditionForUncancellablePayment() {
        @SuppressWarnings("unchecked")
        StreamObserver<CancelPaymentResponse> cancelObserver = mock(StreamObserver.class);

        when(paymentService.cancelPayment("PAY-001", "reason"))
                .thenThrow(new IllegalStateException("Cannot cancel payment in status APPROVED"));

        CancelPaymentRequest request = CancelPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .setReason("reason")
                .build();

        grpcService.cancelPayment(request, cancelObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(cancelObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.FAILED_PRECONDITION);
    }

    // === EDGE CASES - Cenários de produção ===

    @Test
    void shouldRejectEmptyOrderId() {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("CUST001")
                .setOrderId("")  // INVÁLIDO
                .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(10000).build())
                .setMethod(PaymentMethod.PIX)
                .build();

        grpcService.processPayment(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .contains("order_id is required");
    }

    @Test
    void shouldRejectNegativeAmount() {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("CUST001")
                .setOrderId("ORD123")
                .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(-500).build())
                .setMethod(PaymentMethod.CREDIT_CARD)
                .build();

        grpcService.processPayment(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .contains("amount must be greater than zero");
    }

    @Test
    void shouldRejectUnspecifiedPaymentMethod() {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setCustomerId("CUST001")
                .setOrderId("ORD123")
                .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(10000).build())
                .setMethod(PaymentMethod.PAYMENT_METHOD_UNSPECIFIED)
                .build();

        grpcService.processPayment(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .contains("payment_method is required");
    }

    @Test
    void shouldReturnNotFoundWhenCancellingNonExistentPayment() {
        @SuppressWarnings("unchecked")
        StreamObserver<CancelPaymentResponse> cancelObserver = mock(StreamObserver.class);

        when(paymentService.cancelPayment("NON-EXISTENT", "reason"))
                .thenThrow(new IllegalArgumentException("Payment not found: NON-EXISTENT"));

        CancelPaymentRequest request = CancelPaymentRequest.newBuilder()
                .setPaymentId("NON-EXISTENT")
                .setReason("reason")
                .build();

        grpcService.cancelPayment(request, cancelObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(cancelObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void shouldHandleInternalErrorOnGetPayment() {
        GetPaymentRequest request = GetPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .build();

        when(paymentService.getPayment("PAY-001"))
                .thenThrow(new RuntimeException("Database connection lost"));

        grpcService.getPayment(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INTERNAL);
        assertThat(errorCaptor.getValue().getStatus().getDescription())
                .doesNotContain("Database");
    }

    @Test
    void shouldHandleInternalErrorOnCancelPayment() {
        @SuppressWarnings("unchecked")
        StreamObserver<CancelPaymentResponse> cancelObserver = mock(StreamObserver.class);

        when(paymentService.cancelPayment("PAY-001", "reason"))
                .thenThrow(new RuntimeException("Unexpected error"));

        CancelPaymentRequest request = CancelPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .setReason("reason")
                .build();

        grpcService.cancelPayment(request, cancelObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(cancelObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INTERNAL);
    }

    @Test
    void shouldHandleInternalErrorOnListPayments() {
        when(paymentService.getPaymentsByCustomer("CUST001"))
                .thenThrow(new RuntimeException("Repository failure"));

        ListPaymentsRequest request = ListPaymentsRequest.newBuilder()
                .setCustomerId("CUST001")
                .build();

        grpcService.listPayments(request, responseObserver);

        ArgumentCaptor<StatusRuntimeException> errorCaptor =
                ArgumentCaptor.forClass(StatusRuntimeException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getStatus().getCode())
                .isEqualTo(Status.Code.INTERNAL);
    }

    @Test
    void shouldMapAllPaymentMethodsCorrectly() {
        // Testa que todos os métodos de pagamento válidos são mapeados sem exceção
        PaymentMethod[] validMethods = {
                PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_CARD,
                PaymentMethod.PIX, PaymentMethod.BOLETO
        };

        for (PaymentMethod method : validMethods) {
            @SuppressWarnings("unchecked")
            StreamObserver<PaymentResponse> observer = mock(StreamObserver.class);

            Payment domainPayment = Payment.builder()
                    .paymentId(UUID.randomUUID().toString())
                    .status(Payment.PaymentStatus.APPROVED)
                    .createdAt(Instant.now())
                    .build();

            PaymentResponse expectedResponse = PaymentResponse.newBuilder()
                    .setPaymentId(domainPayment.getPaymentId())
                    .build();

            when(paymentService.processPayment(any(), any(), any(), any(), anyMap()))
                    .thenReturn(domainPayment);
            when(mapper.toProto(domainPayment)).thenReturn(expectedResponse);

            PaymentRequest request = PaymentRequest.newBuilder()
                    .setCustomerId("CUST001")
                    .setOrderId("ORD001")
                    .setAmount(Money.newBuilder().setCurrency("BRL").setAmountCents(100).build())
                    .setMethod(method)
                    .build();

            grpcService.processPayment(request, observer);

            verify(observer).onCompleted();
            verify(observer, never()).onError(any());
        }
    }
}
