package com.example.grpc.notification;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service definition
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.62.2)",
    comments = "Source: notification/notification.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class NotificationServiceGrpc {

  private NotificationServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "notification.NotificationService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.grpc.notification.SendNotificationRequest,
      com.example.grpc.notification.NotificationResponse> getSendNotificationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendNotification",
      requestType = com.example.grpc.notification.SendNotificationRequest.class,
      responseType = com.example.grpc.notification.NotificationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.grpc.notification.SendNotificationRequest,
      com.example.grpc.notification.NotificationResponse> getSendNotificationMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.notification.SendNotificationRequest, com.example.grpc.notification.NotificationResponse> getSendNotificationMethod;
    if ((getSendNotificationMethod = NotificationServiceGrpc.getSendNotificationMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getSendNotificationMethod = NotificationServiceGrpc.getSendNotificationMethod) == null) {
          NotificationServiceGrpc.getSendNotificationMethod = getSendNotificationMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.notification.SendNotificationRequest, com.example.grpc.notification.NotificationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendNotification"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.SendNotificationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.NotificationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("SendNotification"))
              .build();
        }
      }
    }
    return getSendNotificationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.grpc.notification.GetNotificationRequest,
      com.example.grpc.notification.NotificationResponse> getGetNotificationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNotification",
      requestType = com.example.grpc.notification.GetNotificationRequest.class,
      responseType = com.example.grpc.notification.NotificationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.grpc.notification.GetNotificationRequest,
      com.example.grpc.notification.NotificationResponse> getGetNotificationMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.notification.GetNotificationRequest, com.example.grpc.notification.NotificationResponse> getGetNotificationMethod;
    if ((getGetNotificationMethod = NotificationServiceGrpc.getGetNotificationMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getGetNotificationMethod = NotificationServiceGrpc.getGetNotificationMethod) == null) {
          NotificationServiceGrpc.getGetNotificationMethod = getGetNotificationMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.notification.GetNotificationRequest, com.example.grpc.notification.NotificationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNotification"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.GetNotificationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.NotificationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("GetNotification"))
              .build();
        }
      }
    }
    return getGetNotificationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.grpc.notification.SendNotificationRequest,
      com.example.grpc.notification.BatchSendResponse> getBatchSendMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchSend",
      requestType = com.example.grpc.notification.SendNotificationRequest.class,
      responseType = com.example.grpc.notification.BatchSendResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.grpc.notification.SendNotificationRequest,
      com.example.grpc.notification.BatchSendResponse> getBatchSendMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.notification.SendNotificationRequest, com.example.grpc.notification.BatchSendResponse> getBatchSendMethod;
    if ((getBatchSendMethod = NotificationServiceGrpc.getBatchSendMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getBatchSendMethod = NotificationServiceGrpc.getBatchSendMethod) == null) {
          NotificationServiceGrpc.getBatchSendMethod = getBatchSendMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.notification.SendNotificationRequest, com.example.grpc.notification.BatchSendResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchSend"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.SendNotificationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.BatchSendResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("BatchSend"))
              .build();
        }
      }
    }
    return getBatchSendMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.grpc.notification.GetNotificationRequest,
      com.example.grpc.notification.NotificationResponse> getMonitorNotificationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MonitorNotifications",
      requestType = com.example.grpc.notification.GetNotificationRequest.class,
      responseType = com.example.grpc.notification.NotificationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.grpc.notification.GetNotificationRequest,
      com.example.grpc.notification.NotificationResponse> getMonitorNotificationsMethod() {
    io.grpc.MethodDescriptor<com.example.grpc.notification.GetNotificationRequest, com.example.grpc.notification.NotificationResponse> getMonitorNotificationsMethod;
    if ((getMonitorNotificationsMethod = NotificationServiceGrpc.getMonitorNotificationsMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getMonitorNotificationsMethod = NotificationServiceGrpc.getMonitorNotificationsMethod) == null) {
          NotificationServiceGrpc.getMonitorNotificationsMethod = getMonitorNotificationsMethod =
              io.grpc.MethodDescriptor.<com.example.grpc.notification.GetNotificationRequest, com.example.grpc.notification.NotificationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MonitorNotifications"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.GetNotificationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.grpc.notification.NotificationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("MonitorNotifications"))
              .build();
        }
      }
    }
    return getMonitorNotificationsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NotificationServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationServiceStub>() {
        @java.lang.Override
        public NotificationServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationServiceStub(channel, callOptions);
        }
      };
    return NotificationServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NotificationServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationServiceBlockingStub>() {
        @java.lang.Override
        public NotificationServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationServiceBlockingStub(channel, callOptions);
        }
      };
    return NotificationServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NotificationServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationServiceFutureStub>() {
        @java.lang.Override
        public NotificationServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationServiceFutureStub(channel, callOptions);
        }
      };
    return NotificationServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service definition
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Unário: enviar notificação única
     * </pre>
     */
    default void sendNotification(com.example.grpc.notification.SendNotificationRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendNotificationMethod(), responseObserver);
    }

    /**
     * <pre>
     * Unário: consultar status
     * </pre>
     */
    default void getNotification(com.example.grpc.notification.GetNotificationRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNotificationMethod(), responseObserver);
    }

    /**
     * <pre>
     * Client streaming: envio em lote
     * </pre>
     */
    default io.grpc.stub.StreamObserver<com.example.grpc.notification.SendNotificationRequest> batchSend(
        io.grpc.stub.StreamObserver<com.example.grpc.notification.BatchSendResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getBatchSendMethod(), responseObserver);
    }

    /**
     * <pre>
     * Bidirecional: monitoramento real-time (opcional, para exemplo)
     * </pre>
     */
    default io.grpc.stub.StreamObserver<com.example.grpc.notification.GetNotificationRequest> monitorNotifications(
        io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getMonitorNotificationsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service NotificationService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static abstract class NotificationServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return NotificationServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service NotificationService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static final class NotificationServiceStub
      extends io.grpc.stub.AbstractAsyncStub<NotificationServiceStub> {
    private NotificationServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NotificationServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unário: enviar notificação única
     * </pre>
     */
    public void sendNotification(com.example.grpc.notification.SendNotificationRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendNotificationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Unário: consultar status
     * </pre>
     */
    public void getNotification(com.example.grpc.notification.GetNotificationRequest request,
        io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNotificationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Client streaming: envio em lote
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.example.grpc.notification.SendNotificationRequest> batchSend(
        io.grpc.stub.StreamObserver<com.example.grpc.notification.BatchSendResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getBatchSendMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Bidirecional: monitoramento real-time (opcional, para exemplo)
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.example.grpc.notification.GetNotificationRequest> monitorNotifications(
        io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getMonitorNotificationsMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service NotificationService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static final class NotificationServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<NotificationServiceBlockingStub> {
    private NotificationServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NotificationServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unário: enviar notificação única
     * </pre>
     */
    public com.example.grpc.notification.NotificationResponse sendNotification(com.example.grpc.notification.SendNotificationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendNotificationMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Unário: consultar status
     * </pre>
     */
    public com.example.grpc.notification.NotificationResponse getNotification(com.example.grpc.notification.GetNotificationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNotificationMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service NotificationService.
   * <pre>
   * Service definition
   * </pre>
   */
  public static final class NotificationServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<NotificationServiceFutureStub> {
    private NotificationServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NotificationServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unário: enviar notificação única
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.grpc.notification.NotificationResponse> sendNotification(
        com.example.grpc.notification.SendNotificationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendNotificationMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Unário: consultar status
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.grpc.notification.NotificationResponse> getNotification(
        com.example.grpc.notification.GetNotificationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNotificationMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND_NOTIFICATION = 0;
  private static final int METHODID_GET_NOTIFICATION = 1;
  private static final int METHODID_BATCH_SEND = 2;
  private static final int METHODID_MONITOR_NOTIFICATIONS = 3;

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
        case METHODID_SEND_NOTIFICATION:
          serviceImpl.sendNotification((com.example.grpc.notification.SendNotificationRequest) request,
              (io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse>) responseObserver);
          break;
        case METHODID_GET_NOTIFICATION:
          serviceImpl.getNotification((com.example.grpc.notification.GetNotificationRequest) request,
              (io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse>) responseObserver);
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
        case METHODID_BATCH_SEND:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.batchSend(
              (io.grpc.stub.StreamObserver<com.example.grpc.notification.BatchSendResponse>) responseObserver);
        case METHODID_MONITOR_NOTIFICATIONS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.monitorNotifications(
              (io.grpc.stub.StreamObserver<com.example.grpc.notification.NotificationResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getSendNotificationMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.grpc.notification.SendNotificationRequest,
              com.example.grpc.notification.NotificationResponse>(
                service, METHODID_SEND_NOTIFICATION)))
        .addMethod(
          getGetNotificationMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.grpc.notification.GetNotificationRequest,
              com.example.grpc.notification.NotificationResponse>(
                service, METHODID_GET_NOTIFICATION)))
        .addMethod(
          getBatchSendMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              com.example.grpc.notification.SendNotificationRequest,
              com.example.grpc.notification.BatchSendResponse>(
                service, METHODID_BATCH_SEND)))
        .addMethod(
          getMonitorNotificationsMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              com.example.grpc.notification.GetNotificationRequest,
              com.example.grpc.notification.NotificationResponse>(
                service, METHODID_MONITOR_NOTIFICATIONS)))
        .build();
  }

  private static abstract class NotificationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NotificationServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.grpc.notification.NotificationProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NotificationService");
    }
  }

  private static final class NotificationServiceFileDescriptorSupplier
      extends NotificationServiceBaseDescriptorSupplier {
    NotificationServiceFileDescriptorSupplier() {}
  }

  private static final class NotificationServiceMethodDescriptorSupplier
      extends NotificationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    NotificationServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (NotificationServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NotificationServiceFileDescriptorSupplier())
              .addMethod(getSendNotificationMethod())
              .addMethod(getGetNotificationMethod())
              .addMethod(getBatchSendMethod())
              .addMethod(getMonitorNotificationsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
