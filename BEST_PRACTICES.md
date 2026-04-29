# Padrões e Boas Práticas - gRPC em Produção

## Decisões Arquiteturais

### 1. Contratos em Módulo Separado

**Por quê:**
- Versionamento independente
- Reutilização entre múltiplos serviços
- Fonte única de verdade
- Facilita breaking change detection

**Como:**
```
proto-contracts/     <- módulo compartilhado
  └── build/
      └── libs/
          └── proto-contracts-1.0.0.jar  <- publicar no Nexus/Artifactory

payment-service/
  └── build.gradle
      dependencies {
          implementation 'com.example:proto-contracts:1.0.0'
      }
```

### 2. Sempre Definir Deadlines

**Problema:** Sem deadline, chamadas gRPC podem travar indefinidamente.

**Solução:**
```java
// ERRADO - sem deadline
stub.processPayment(request);

// CORRETO - sempre com deadline
stub.withDeadline(5, TimeUnit.SECONDS)
    .processPayment(request);
```

**Estratégia:**
- APIs síncronas críticas: 3-5s
- Background jobs: 30-60s
- Streaming: sem deadline ou muito longo (10m+)

### 3. Circuit Breaker + Retry

**Por quê:** Evita cascata de falhas e melhora resiliência.

**Configuração típica:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      notification:
        sliding-window-size: 10           # últimas 10 chamadas
        failure-rate-threshold: 50        # abre se >50% falharem
        wait-duration-in-open-state: 10s  # tempo no estado aberto
        
  retry:
    instances:
      notification:
        max-attempts: 3                   # 3 tentativas
        wait-duration: 500ms              # espera entre retries
        exponential-backoff-multiplier: 2 # backoff exponencial
```

**Estados do Circuit Breaker:**
```
CLOSED → chamadas normais
  ↓ (muitas falhas)
OPEN → rejeita imediatamente (fast-fail)
  ↓ (após wait-duration)
HALF_OPEN → permite algumas chamadas de teste
  ↓ (sucesso) / (falha)
CLOSED / OPEN
```

### 4. Logging Estruturado com MDC

**Por quê:** Correlacionar logs entre microserviços.

```java
// No interceptor
MDC.put("request-id", requestId);
MDC.put("grpc-method", methodName);

log.info("Processing payment");  
// Output: 2024-04-22 10:15:30 request-id=abc123 grpc-method=ProcessPayment Processing payment

// Cleanup ao final
MDC.clear();
```

**Propagar entre serviços:**
```java
// Client
Metadata headers = new Metadata();
headers.put(REQUEST_ID_KEY, MDC.get("request-id"));
stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

// Server
String requestId = headers.get(REQUEST_ID_KEY);
MDC.put("request-id", requestId);
```

### 5. Error Handling por Status Code

**Mapeamento Java Exception → gRPC Status:**

| Exception Java | gRPC Status | Quando usar |
|---|---|---|
| `IllegalArgumentException` | `INVALID_ARGUMENT` | Input inválido |
| `IllegalStateException` | `FAILED_PRECONDITION` | Estado não permite operação |
| `NullPointerException` | `INTERNAL` | Nunca expor NPE! |
| Custom `NotFoundException` | `NOT_FOUND` | Recurso não existe |
| Custom `UnauthorizedException` | `UNAUTHENTICATED` | Token inválido |
| Custom `ForbiddenException` | `PERMISSION_DENIED` | Sem permissão |
| `TimeoutException` | `DEADLINE_EXCEEDED` | Timeout |

**Erro com metadata:**
```java
ErrorDetail detail = ErrorDetail.newBuilder()
    .setCode("PAYMENT_REJECTED")
    .setMessage("Insufficient funds")
    .putMetadata("available_balance", "100.00")
    .build();

Metadata trailers = new Metadata();
trailers.put(ERROR_DETAIL_KEY, detail.toByteArray());

throw Status.FAILED_PRECONDITION
    .withDescription("Payment rejected")
    .asRuntimeException(trailers);
```

### 6. Evolução de Contrato Segura

** Breaking Changes (evitar):**
```protobuf
// ANTES
message User {
  string name = 1;
  int32 age = 2;
}

// DEPOIS - QUEBRA COMPATIBILIDADE!
message User {
  string name = 1;
  string age = 2;  // mudou de int32 para string
}
```

** Evolução Segura:**
```protobuf
// V1
message User {
  string name = 1;
  int32 age = 2;
}

// V2 - compatível
message User {
  string name = 1;
  int32 age = 2;
  optional string email = 3;     // campo novo (opcional)
  reserved 4;                    // reserva slot para futuro
  reserved "deprecated_field";   // nome nunca será reusado
}
```

### 7. Streaming: Quando Usar

| Padrão | Use quando | Evite quando |
|---|---|---|
| **Unário** | CRUD, validação, 90% dos casos | Dados grandes |
| **Server stream** | Paginação, logs, eventos | Cliente precisa de tudo de uma vez |
| **Client stream** | Upload de arquivo, batch insert | Cada item é independente |
| **Bidirecional** | Chat, sync real-time | Lógica pode ser unária |

**Server streaming:**
```java
public void listOrders(ListRequest req, StreamObserver<Order> observer) {
    repository.findAll().forEach(order -> {
        observer.onNext(order);  // envia cada item
    });
    observer.onCompleted();
}
```

### 8. Monitoramento e Observabilidade

**Métricas essenciais (Prometheus):**
```
# Latência
grpc_server_handling_seconds_bucket{grpc_method="ProcessPayment"}

# Taxa de erro
rate(grpc_server_handled_total{grpc_code!="OK"}[5m])

# Throughput
rate(grpc_server_started_total[1m])

# Circuit Breaker
resilience4j_circuitbreaker_state{name="notification"}
```

**Alertas recomendados:**
```yaml
# SLO: 99% das chamadas < 500ms
- alert: HighLatency
  expr: histogram_quantile(0.99, grpc_server_handling_seconds_bucket) > 0.5

# SLO: < 1% de erro
- alert: HighErrorRate
  expr: rate(grpc_server_handled_total{grpc_code!="OK"}[5m]) > 0.01

# Circuit Breaker aberto
- alert: CircuitBreakerOpen
  expr: resilience4j_circuitbreaker_state{state="open"} == 1
```

### 9. Testes

**Unit test do gRPC service:**
```java
@Test
void shouldProcessPayment() {
    PaymentRequest request = PaymentRequest.newBuilder()
        .setCustomerId("CUST001")
        .setAmount(Money.newBuilder().setAmountCents(10000).build())
        .build();
    
    StreamObserver<PaymentResponse> observer = mock(StreamObserver.class);
    
    service.processPayment(request, observer);
    
    verify(observer).onNext(argThat(response -> 
        response.getStatus() == PaymentStatus.APPROVED
    ));
    verify(observer).onCompleted();
}
```

**Integration test com in-process server:**
```java
@SpringBootTest
@GrpcServerTest
class PaymentGrpcIntegrationTest {
    
    @GrpcClient("inProcess")
    private PaymentServiceBlockingStub stub;
    
    @Test
    void shouldCallRealGrpcService() {
        PaymentResponse response = stub.processPayment(request);
        assertThat(response.getStatus()).isEqualTo(APPROVED);
    }
}
```

### 10. Segurança em Produção

**Checklist:**
- [ ] TLS habilitado (certificados válidos)
- [ ] mTLS para service-to-service
- [ ] JWT validation no interceptor
- [ ] Rate limiting por cliente
- [ ] Input validation em todos os RPCs
- [ ] Não expor stack traces no erro
- [ ] Audit log de operações sensíveis

**Interceptor de autenticação:**
```java
@Override
public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    
    String token = headers.get(AUTH_TOKEN_KEY);
    
    if (!jwtValidator.isValid(token)) {
        call.close(Status.UNAUTHENTICATED
            .withDescription("Invalid token"), new Metadata());
        return new ServerCall.Listener<>() {};
    }
    
    return next.startCall(call, headers);
}
```

---

## Recursos Adicionais

- **Buf.build** - Lint e breaking change detection para .proto
- **grpc-gateway** - Gera REST API a partir de .proto
- **Envoy** - Service mesh com observability
- **OpenTelemetry** - Tracing distribuído
