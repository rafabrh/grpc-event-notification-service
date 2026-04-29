package com.example.payment.interceptor;

import com.example.grpc.common.ErrorDetail;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * Interceptor para tratamento centralizado de exceções.
 * 
 * Converte exceções Java em Status gRPC apropriados com metadata estruturada.
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class ExceptionHandlerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    handleException(e, call, headers);
                }
            }
        };
    }

    private <ReqT, RespT> void handleException(Exception e, ServerCall<ReqT, RespT> call, Metadata metadata) {
        Status status;
        Metadata trailers = new Metadata();

        if (e instanceof IllegalArgumentException) {
            status = Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .withCause(e);
            
            attachErrorDetail(trailers, "INVALID_ARGUMENT", e.getMessage());
            
        } else if (e instanceof IllegalStateException) {
            status = Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .withCause(e);
            
            attachErrorDetail(trailers, "FAILED_PRECONDITION", e.getMessage());
            
        } else if (e instanceof StatusRuntimeException) {
            // Já é um erro gRPC, apenas repropaga
            StatusRuntimeException sre = (StatusRuntimeException) e;
            call.close(sre.getStatus(), sre.getTrailers());
            return;
            
        } else {
            // Erro genérico/não mapeado
            log.error("Unhandled exception in gRPC call", e);
            status = Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e);
            
            attachErrorDetail(trailers, "INTERNAL_ERROR", "An unexpected error occurred");
        }

        call.close(status, trailers);
    }

    private void attachErrorDetail(Metadata trailers, String code, String message) {
        ErrorDetail errorDetail = ErrorDetail.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();

        Metadata.Key<byte[]> errorKey = Metadata.Key.of(
                "error-detail-bin",
                Metadata.BINARY_BYTE_MARSHALLER
        );
        
        trailers.put(errorKey, errorDetail.toByteArray());
    }
}
