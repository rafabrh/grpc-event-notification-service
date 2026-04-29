package com.example.payment.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Interceptor global para logging estruturado, tracing distribuído e error handling.
 * 
 * Patterns implementados:
 * - Request ID para correlação de logs
 * - MDC (Mapped Diagnostic Context) para logging estruturado
 * - Exceções padronizadas com metadata
 * - Latency tracking
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class LoggingInterceptor implements ServerInterceptor {

    private static final String REQUEST_ID_KEY = "request-id";
    private static final String METHOD_KEY = "grpc-method";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Gerar ou extrair request ID para correlação
        String requestId = extractOrGenerateRequestId(headers);
        String methodName = call.getMethodDescriptor().getFullMethodName();
        
        // Adiciona ao MDC para aparecer em todos os logs dessa thread
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(METHOD_KEY, methodName);

        long startTime = System.currentTimeMillis();
        
        log.info("gRPC request started: method={}", methodName);

        // Wrapper para capturar resposta/erro e calcular latência
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            
            @Override
            public void close(Status status, Metadata trailers) {
                long latency = System.currentTimeMillis() - startTime;
                
                if (status.isOk()) {
                    log.info("gRPC request completed: method={}, latency={}ms, status=OK", 
                            methodName, latency);
                } else {
                    log.error("gRPC request failed: method={}, latency={}ms, status={}, description={}", 
                            methodName, latency, status.getCode(), status.getDescription());
                }
                
                // Limpa MDC ao final
                MDC.clear();
                
                // Adiciona request-id no trailer para client tracking
                trailers.put(Metadata.Key.of(REQUEST_ID_KEY, Metadata.ASCII_STRING_MARSHALLER), requestId);
                
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }

    private String extractOrGenerateRequestId(Metadata headers) {
        Metadata.Key<String> requestIdKey = Metadata.Key.of(REQUEST_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
        String requestId = headers.get(requestIdKey);
        
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        
        return requestId;
    }
}
