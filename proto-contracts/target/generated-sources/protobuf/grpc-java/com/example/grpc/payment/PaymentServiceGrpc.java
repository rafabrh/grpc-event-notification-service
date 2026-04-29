package com.example.grpc.payment;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service definition
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.62.2)",
    comments = "Source: payment/payment.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PaymentServiceGrpc {

  private PaymentServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "payment.PaymentService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.grpc.payment.PaymentRequest,
      com.example.grpc.payment.PaymentResponse> getProcessPaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ProcessPayment",
      requestType = com.example.grpc.payment.PaymentRequest.class,
      responseType = com.example.grpc.payment.PaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.grpc.payment.PaymentRequest,
      com.example.grpc.payment.PaymentResponse> getProcessPaymentMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.payment.PaymentRequest, com.example.grpc.payment.PaymentResponse> getProcessPaymentMethod;
    if ((getProcessPaymentMethod = PaymentServiceGrpc.getProcessPaymentMethod) == null) {
      synchronized (PaymentServiceGrpc.class) {
        if ((getProcessPaymentMethod = PaymentServiceGrpc.getProcessPaymentMethod) == null) {
          PaymentServiceGrpc.getProcessPaymentMethod = getProcessPaymentMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.payment.PaymentRequest, com.example.grpc.payment.PaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ProcessPayment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.PaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.PaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PaymentServiceMethodDescriptorSupplier("ProcessPayment"))
              .build();
        }
      }
    }
    return getProcessPaymentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.grpc.payment.GetPaymentRequest,
      com.example.grpc.payment.PaymentResponse> getGetPaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPayment",
      requestType = com.example.grpc.payment.GetPaymentRequest.class,
      responseType = com.example.grpc.payment.PaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.grpc.payment.GetPaymentRequest,
      com.example.grpc.payment.PaymentResponse> getGetPaymentMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.payment.GetPaymentRequest, com.example.grpc.payment.PaymentResponse> getGetPaymentMethod;
    if ((getGetPaymentMethod = PaymentServiceGrpc.getGetPaymentMethod) == null) {
      synchronized (PaymentServiceGrpc.class) {
        if ((getGetPaymentMethod = PaymentServiceGrpc.getGetPaymentMethod) == null) {
          PaymentServiceGrpc.getGetPaymentMethod = getGetPaymentMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.payment.GetPaymentRequest, com.example.grpc.payment.PaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPayment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.GetPaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.PaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PaymentServiceMethodDescriptorSupplier("GetPayment"))
              .build();
        }
      }
    }
    return getGetPaymentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.grpc.payment.ListPaymentsRequest,
      com.example.grpc.payment.PaymentResponse> getListPaymentsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListPayments",
      requestType = com.example.grpc.payment.ListPaymentsRequest.class,
      responseType = com.example.grpc.payment.PaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.grpc.payment.ListPaymentsRequest,
      com.example.grpc.payment.PaymentResponse> getListPaymentsMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.payment.ListPaymentsRequest, com.example.grpc.payment.PaymentResponse> getListPaymentsMethod;
    if ((getListPaymentsMethod = PaymentServiceGrpc.getListPaymentsMethod) == null) {
      synchronized (PaymentServiceGrpc.class) {
        if ((getListPaymentsMethod = PaymentServiceGrpc.getListPaymentsMethod) == null) {
          PaymentServiceGrpc.getListPaymentsMethod = getListPaymentsMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.payment.ListPaymentsRequest, com.example.grpc.payment.PaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListPayments"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.ListPaymentsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.PaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PaymentServiceMethodDescriptorSupplier("ListPayments"))
              .build();
        }
      }
    }
    return getListPaymentsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.grpc.payment.CancelPaymentRequest,
      com.example.grpc.payment.CancelPaymentResponse> getCancelPaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelPayment",
      requestType = com.example.grpc.payment.CancelPaymentRequest.class,
      responseType = com.example.grpc.payment.CancelPaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.grpc.payment.CancelPaymentRequest,
      com.example.grpc.payment.CancelPaymentResponse> getCancelPaymentMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.payment.CancelPaymentRequest, com.example.grpc.payment.CancelPaymentResponse> getCancelPaymentMethod;
    if ((getCancelPaymentMethod = PaymentServiceGrpc.getCancelPaymentMethod) == null) {
      synchronized (PaymentServiceGrpc.class) {
        if ((getCancelPaymentMethod = PaymentServiceGrpc.getCancelPaymentMethod) == null) {
          PaymentServiceGrpc.getCancelPaymentMethod = getCancelPaymentMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.payment.CancelPaymentRequest, com.example.grpc.payment.CancelPaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelPayment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.CancelPaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.payment.CancelPaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PaymentServiceMethodDescriptorSupplier("CancelPayment"))
              .build();
        }
      }
    }
    return getCancelPaymentMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PaymentServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaymentServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaymentServiceStub>() {
        @java.lang.Override
        public PaymentServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaymentServiceStub(channel, callOptions);
        }
      };
    return PaymentServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PaymentServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaymentServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaymentServiceBlockingStub>() {
        @java.lang.Override
        public PaymentServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaymentServiceBlockingStub(channel, callOptions);
        }
      };
    return PaymentServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PaymentServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaymentServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaymentServiceFutureStub>() {
        @java.lang.Override
        public PaymentServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaymentServiceFutureStub(channel, callOptions);
        }
      };
    return PaymentServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service definition
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Unário: criar pagamento
     * </pre>
     */
    default void processPayment(com.example.grpc.payment.PaymentRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProcessPaymentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Unário: consultar pagamento
     * </pre>
     */
    default void getPayment(com.example.grpc.payment.GetPaymentRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPaymentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Server streaming: listar pagamentos paginados
     * </pre>
     */
    default void listPayments(com.example.grpc.payment.ListPaymentsRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListPaymentsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Unário: cancelar pagamento
     * </pre>
     */
    default void cancelPayment(com.example.grpc.payment.CancelPaymentRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.CancelPaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelPaymentMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PaymentService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static abstract class PaymentServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PaymentServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PaymentService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static final class PaymentServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PaymentServiceStub> {
    private PaymentServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaymentServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaymentServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unário: criar pagamento
     * </pre>
     */
    public void processPayment(com.example.grpc.payment.PaymentRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProcessPaymentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Unário: consultar pagamento
     * </pre>
     */
    public void getPayment(com.example.grpc.payment.GetPaymentRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPaymentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Server streaming: listar pagamentos paginados
     * </pre>
     */
    public void listPayments(com.example.grpc.payment.ListPaymentsRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getListPaymentsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Unário: cancelar pagamento
     * </pre>
     */
    public void cancelPayment(com.example.grpc.payment.CancelPaymentRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.payment.CancelPaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelPaymentMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PaymentService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static final class PaymentServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PaymentServiceBlockingStub> {
    private PaymentServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaymentServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaymentServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unário: criar pagamento
     * </pre>
     */
    public com.example.grpc.payment.PaymentResponse processPayment(com.example.grpc.payment.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProcessPaymentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Unário: consultar pagamento
     * </pre>
     */
    public com.example.grpc.payment.PaymentResponse getPayment(com.example.grpc.payment.GetPaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPaymentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Server streaming: listar pagamentos paginados
     * </pre>
     */
    public java.util.Iterator<com.example.grpc.payment.PaymentResponse> listPayments(
        com.example.grpc.payment.ListPaymentsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getListPaymentsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Unário: cancelar pagamento
     * </pre>
     */
    public com.example.grpc.payment.CancelPaymentResponse cancelPayment(com.example.grpc.payment.CancelPaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelPaymentMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PaymentService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static final class PaymentServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PaymentServiceFutureStub> {
    private PaymentServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaymentServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaymentServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unário: criar pagamento
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.grpc.payment.PaymentResponse> processPayment(
        com.example.grpc.payment.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProcessPaymentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Unário: consultar pagamento
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.grpc.payment.PaymentResponse> getPayment(
        com.example.grpc.payment.GetPaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPaymentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Unário: cancelar pagamento
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.grpc.payment.CancelPaymentResponse> cancelPayment(
        com.example.grpc.payment.CancelPaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelPaymentMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROCESS_PAYMENT = 0;
  private static final int METHODID_GET_PAYMENT = 1;
  private static final int METHODID_LIST_PAYMENTS = 2;
  private static final int METHODID_CANCEL_PAYMENT = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PROCESS_PAYMENT:
          serviceImpl.processPayment((com.example.grpc.payment.PaymentRequest) request,
              (io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse>) responseObserver);
          break;
        case METHODID_GET_PAYMENT:
          serviceImpl.getPayment((com.example.grpc.payment.GetPaymentRequest) request,
              (io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse>) responseObserver);
          break;
        case METHODID_LIST_PAYMENTS:
          serviceImpl.listPayments((com.example.grpc.payment.ListPaymentsRequest) request,
              (io.grpc.stub.StreamObserver<com.example.grpc.payment.PaymentResponse>) responseObserver);
          break;
        case METHODID_CANCEL_PAYMENT:
          serviceImpl.cancelPayment((com.example.grpc.payment.CancelPaymentRequest) request,
              (io.grpc.stub.StreamObserver<com.example.grpc.payment.CancelPaymentResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getProcessPaymentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.grpc.payment.PaymentRequest,
              com.example.grpc.payment.PaymentResponse>(
                service, METHODID_PROCESS_PAYMENT)))
        .addMethod(
          getGetPaymentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.grpc.payment.GetPaymentRequest,
              com.example.grpc.payment.PaymentResponse>(
                service, METHODID_GET_PAYMENT)))
        .addMethod(
          getListPaymentsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.example.grpc.payment.ListPaymentsRequest,
              com.example.grpc.payment.PaymentResponse>(
                service, METHODID_LIST_PAYMENTS)))
        .addMethod(
          getCancelPaymentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.grpc.payment.CancelPaymentRequest,
              com.example.grpc.payment.CancelPaymentResponse>(
                service, METHODID_CANCEL_PAYMENT)))
        .build();
  }

  private static abstract class PaymentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PaymentServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.grpc.payment.PaymentProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PaymentService");
    }
  }

  private static final class PaymentServiceFileDescriptorSupplier
      extends PaymentServiceBaseDescriptorSupplier {
    PaymentServiceFileDescriptorSupplier() {}
  }

  private static final class PaymentServiceMethodDescriptorSupplier
      extends PaymentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PaymentServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PaymentServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PaymentServiceFileDescriptorSupplier())
              .addMethod(getProcessPaymentMethod())
              .addMethod(getGetPaymentMethod())
              .addMethod(getListPaymentsMethod())
              .addMethod(getCancelPaymentMethod())
              .build();
        }
      }
    }
    return result;
  }
}
