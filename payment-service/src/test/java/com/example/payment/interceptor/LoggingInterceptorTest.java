package com.example.payment.interceptor;

import io.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingInterceptorTest {

    private LoggingInterceptor interceptor;

    @Mock
    private ServerCall<Object, Object> serverCall;

    @Mock
    private ServerCallHandler<Object, Object> callHandler;

    @Mock
    private ServerCall.Listener<Object> listener;

    private static final Metadata.Key<String> REQUEST_ID_KEY =
            Metadata.Key.of("request-id", Metadata.ASCII_STRING_MARSHALLER);

    @BeforeEach
    void setUp() {
        interceptor = new LoggingInterceptor();
    }

    @Test
    void shouldGenerateRequestIdWhenNotProvided() {
        Metadata headers = new Metadata();
        MethodDescriptor<Object, Object> methodDescriptor = createMethodDescriptor();
        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        when(callHandler.startCall(any(), any())).thenReturn(listener);

        interceptor.interceptCall(serverCall, headers, callHandler);

        // Verifica que o handler foi chamado com o call wrapped
        verify(callHandler).startCall(any(), eq(headers));
    }

    @Test
    void shouldUseExistingRequestIdFromHeaders() {
        Metadata headers = new Metadata();
        headers.put(REQUEST_ID_KEY, "existing-request-id");
        MethodDescriptor<Object, Object> methodDescriptor = createMethodDescriptor();
        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        when(callHandler.startCall(any(), any())).thenReturn(listener);

        interceptor.interceptCall(serverCall, headers, callHandler);

        verify(callHandler).startCall(any(), eq(headers));
    }

    @Test
    void shouldAddRequestIdToTrailersOnClose() {
        Metadata headers = new Metadata();
        MethodDescriptor<Object, Object> methodDescriptor = createMethodDescriptor();
        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);

        // Captura o wrapped call
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ServerCall<Object, Object>> callCaptor =
                ArgumentCaptor.forClass(ServerCall.class);
        when(callHandler.startCall(callCaptor.capture(), any())).thenReturn(listener);

        interceptor.interceptCall(serverCall, headers, callHandler);

        // Simula close da chamada
        ServerCall<Object, Object> wrappedCall = callCaptor.getValue();
        Metadata trailers = new Metadata();
        wrappedCall.close(Status.OK, trailers);

        // Verifica que request-id foi adicionado aos trailers
        String requestId = trailers.get(REQUEST_ID_KEY);
        assertThat(requestId).isNotNull().isNotBlank();

        verify(serverCall).close(eq(Status.OK), any(Metadata.class));
    }

    @Test
    void shouldPreserveProvidedRequestIdInTrailers() {
        Metadata headers = new Metadata();
        headers.put(REQUEST_ID_KEY, "my-trace-id-123");
        MethodDescriptor<Object, Object> methodDescriptor = createMethodDescriptor();
        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ServerCall<Object, Object>> callCaptor =
                ArgumentCaptor.forClass(ServerCall.class);
        when(callHandler.startCall(callCaptor.capture(), any())).thenReturn(listener);

        interceptor.interceptCall(serverCall, headers, callHandler);

        ServerCall<Object, Object> wrappedCall = callCaptor.getValue();
        Metadata trailers = new Metadata();
        wrappedCall.close(Status.OK, trailers);

        assertThat(trailers.get(REQUEST_ID_KEY)).isEqualTo("my-trace-id-123");
    }

    @Test
    void shouldLogErrorStatusOnClose() {
        Metadata headers = new Metadata();
        MethodDescriptor<Object, Object> methodDescriptor = createMethodDescriptor();
        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ServerCall<Object, Object>> callCaptor =
                ArgumentCaptor.forClass(ServerCall.class);
        when(callHandler.startCall(callCaptor.capture(), any())).thenReturn(listener);

        interceptor.interceptCall(serverCall, headers, callHandler);

        ServerCall<Object, Object> wrappedCall = callCaptor.getValue();
        Metadata trailers = new Metadata();
        Status errorStatus = Status.INTERNAL.withDescription("Something went wrong");
        wrappedCall.close(errorStatus, trailers);

        // Verifica que close foi delegado com o status de erro
        verify(serverCall).close(eq(errorStatus), any(Metadata.class));
    }

    @Test
    void shouldHandleBlankRequestIdAsNewGeneration() {
        Metadata headers = new Metadata();
        headers.put(REQUEST_ID_KEY, "   ");
        MethodDescriptor<Object, Object> methodDescriptor = createMethodDescriptor();
        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ServerCall<Object, Object>> callCaptor =
                ArgumentCaptor.forClass(ServerCall.class);
        when(callHandler.startCall(callCaptor.capture(), any())).thenReturn(listener);

        interceptor.interceptCall(serverCall, headers, callHandler);

        ServerCall<Object, Object> wrappedCall = callCaptor.getValue();
        Metadata trailers = new Metadata();
        wrappedCall.close(Status.OK, trailers);

        // Deve gerar um novo UUID, não usar o blank
        String requestId = trailers.get(REQUEST_ID_KEY);
        assertThat(requestId).isNotBlank().isNotEqualTo("   ");
    }

    @SuppressWarnings("unchecked")
    private MethodDescriptor<Object, Object> createMethodDescriptor() {
        return (MethodDescriptor<Object, Object>) MethodDescriptor.newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName("payment.PaymentService/ProcessPayment")
                .setRequestMarshaller(mock(MethodDescriptor.Marshaller.class))
                .setResponseMarshaller(mock(MethodDescriptor.Marshaller.class))
                .build();
    }
}
