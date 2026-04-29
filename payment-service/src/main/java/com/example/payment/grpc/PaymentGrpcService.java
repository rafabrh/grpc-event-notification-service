package com.example.payment.grpc;

import com.example.grpc.payment.*;
import com.example.payment.domain.Payment;
import com.example.payment.mapper.PaymentMapper;
import com.example.payment.service.PaymentService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Implementação gRPC do PaymentService.
 * 
 * Patterns implementados:
 * - Validação de input
 * - Error handling com status codes apropriados
 * - Logging estruturado
 * - Server streaming para listagens
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {
    
    private final PaymentService paymentService;
    private final PaymentMapper mapper;
    
    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("gRPC ProcessPayment called: customerId={}, orderId={}", 
                request.getCustomerId(), request.getOrderId());
        
        try {
            // Validação
            validatePaymentRequest(request);
            
            // Processar
            Payment.Money amount = Payment.Money.builder()
                    .currency(request.getAmount().getCurrency())
                    .amountCents(request.getAmount().getAmountCents())
                    .build();
            
            Payment payment = paymentService.processPayment(
                    request.getCustomerId(),
                    request.getOrderId(),
                    amount,
                    mapPaymentMethod(request.getMethod()),
                    request.getMetadataMap()
            );
            
            // Responder
            PaymentResponse response = mapper.toProto(payment);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("Payment processed successfully: paymentId={}, status={}", 
                    payment.getPaymentId(), payment.getStatus());
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment request: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
                    
        } catch (Exception e) {
            log.error("Error processing payment", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getPayment(GetPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("gRPC GetPayment called: paymentId={}", request.getPaymentId());
        
        try {
            Payment payment = paymentService.getPayment(request.getPaymentId());
            
            PaymentResponse response = mapper.toProto(payment);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", request.getPaymentId());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
                    
        } catch (Exception e) {
            log.error("Error getting payment", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
    
    @Override
    public void listPayments(ListPaymentsRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("gRPC ListPayments called: customerId={}, page={}",
                request.getCustomerId(), request.getPage().getPage());

        try {
            if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("customer_id is required")
                        .asRuntimeException());
                return;
            }

            java.util.List<com.example.payment.domain.Payment> payments =
                    paymentService.getPaymentsByCustomer(request.getCustomerId());

            for (com.example.payment.domain.Payment payment : payments) {
                responseObserver.onNext(mapper.toProto(payment));
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error listing payments", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
    
    @Override
    public void cancelPayment(CancelPaymentRequest request, StreamObserver<CancelPaymentResponse> responseObserver) {
        log.info("gRPC CancelPayment called: paymentId={}", request.getPaymentId());
        
        try {
            Payment payment = paymentService.cancelPayment(
                    request.getPaymentId(), 
                    request.getReason()
            );
            
            CancelPaymentResponse response = CancelPaymentResponse.newBuilder()
                    .setPaymentId(payment.getPaymentId())
                    .setStatus(mapper.toProto(payment).getStatus())
                    .setMetadata(mapper.toProto(payment).getMetadata())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
                    
        } catch (IllegalStateException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
                    
        } catch (Exception e) {
            log.error("Error cancelling payment", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
    
    // Helpers
    
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("customer_id is required");
        }
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new IllegalArgumentException("order_id is required");
        }
        if (request.getAmount().getAmountCents() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (request.getMethod() == PaymentMethod.PAYMENT_METHOD_UNSPECIFIED) {
            throw new IllegalArgumentException("payment_method is required");
        }
    }
    
    private Payment.PaymentMethod mapPaymentMethod(PaymentMethod protoMethod) {
        return switch (protoMethod) {
            case CREDIT_CARD -> Payment.PaymentMethod.CREDIT_CARD;
            case DEBIT_CARD -> Payment.PaymentMethod.DEBIT_CARD;
            case PIX -> Payment.PaymentMethod.PIX;
            case BOLETO -> Payment.PaymentMethod.BOLETO;
            default -> throw new IllegalArgumentException("Invalid payment method");
        };
    }
}
