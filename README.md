# gRPC Microservices - Payment & Notification System

Sistema de microservicos comunicando via gRPC.

## Arquitetura

```
                         VISAO GERAL DO SISTEMA
 ___________________________________________________________________________

   Postman / grpcurl                 Postman / grpcurl
        |                                  |
        | gRPC (porta 9090)                | gRPC (porta 9091)
        v                                  v
 +-------------------------------+   +-------------------------------+
 |       PAYMENT SERVICE         |   |    NOTIFICATION SERVICE       |
 |                               |   |                               |
 |  +-------------------------+  |   |  +-------------------------+  |
 |  |     Interceptors        |  |   |  |    gRPC Layer           |  |
 |  |  LoggingInterceptor     |  |   |  |  NotificationGrpc-      |  |
 |  |  ExceptionHandler-      |  |   |  |  Service                |  |
 |  |  Interceptor            |  |   |  +----------+--------------+  |
 |  +----------+--------------+  |   |             |                 |
 |             |                 |   |             v                 |
 |             v                 |   |  +-------------------------+  |
 |  +-------------------------+  |   |  |   NotificationService   |  |
 |  |  PaymentGrpcService     |  |   |  |   (logica de negocio)   |  |
 |  |  (camada gRPC)          |  |   |  +----------+--------------+  |
 |  +----------+--------------+  |   |             |                 |
 |             |                 |   |             v                 |
 |             v                 |   |  +-------------------------+  |
 |  +-------------------------+  |   |  | NotificationRepository  |  |
 |  |    PaymentService       |  |   |  |     (in-memory)         |  |
 |  |  (logica de negocio)    |  |   |  +-------------------------+  |
 |  +-----+-------------+----+  |   +-------------------------------+
 |        |             |        |                  ^
 |        v             v        |                  |
 |  +-----------+ +----------+   |    gRPC call     |
 |  | Payment-  | | Payment- |   |  (porta 9091)    |
 |  | Repository| | Notific- |   |                  |
 |  | (memory)  | | ation-   +---|------------------+
 |  +-----------+ | Sender   |   |  @CircuitBreaker
 |                | @Retry   |   |  @Retry
 |                +-----+----+   |  Deadline: 5s
 |                      |        |
 |                      v        |
 |               +------------+  |
 |               | Notifica-  |  |
 |               | tionClient |  |
 |               | (gRPC stub)|  |
 |               +------------+  |
 +-------------------------------+
```

### Fluxo de um Pagamento (passo a passo)

```
 Cliente                Payment Service                    Notification Service
    |                        |                                     |
    |  1. ProcessPayment()   |                                     |
    |----------------------->|                                     |
    |                        |                                     |
    |                  2. LoggingInterceptor                       |
    |                     gera request-id                          |
    |                     inicia timer                             |
    |                        |                                     |
    |                  3. PaymentGrpcService                       |
    |                     valida input                             |
    |                     converte proto -> domain                 |
    |                        |                                     |
    |                  4. PaymentService                           |
    |                     cria Payment                             |
    |                     simula aprovacao/rejeicao                |
    |                     salva no repository                      |
    |                        |                                     |
    |                  5. PaymentNotificationSender                |
    |                     @CircuitBreaker + @Retry                 |
    |                        |                                     |
    |                        |  6. SendNotification()              |
    |                        |------------------------------------>|
    |                        |                                     |
    |                        |     7. NotificationResponse         |
    |                        |<------------------------------------|
    |                        |                                     |
    |  8. PaymentResponse    |                                     |
    |<-----------------------|                                     |
    |                        |                                     |
    |                  9. LoggingInterceptor                       |
    |                     calcula latencia                         |
    |                     adiciona request-id nos trailers         |
```

## Componentes e o que cada um faz

### Payment Service

| Componente | Responsabilidade |
|---|---|
| `LoggingInterceptor` | Gera/propaga request-id, adiciona MDC, mede latencia |
| `ExceptionHandlerInterceptor` | Converte excecoes Java em Status gRPC (INVALID_ARGUMENT, NOT_FOUND, etc) |
| `PaymentGrpcService` | Recebe chamadas gRPC, valida input, delega para service layer |
| `PaymentService` | Logica de negocio: cria pagamento, simula aprovacao, gerencia cancelamento |
| `PaymentNotificationSender` | Envia notificacao com Circuit Breaker + Retry + Fallback |
| `NotificationClient` | Stub gRPC para chamar NotificationService com deadline de 5s |
| `PaymentMapper` | Converte entre proto messages e domain objects |
| `PaymentRepository` | Armazena pagamentos em memoria (ConcurrentHashMap) |

### Notification Service

| Componente | Responsabilidade |
|---|---|
| `NotificationGrpcService` | Recebe chamadas gRPC (unario, client streaming, bidirecional) |
| `NotificationService` | Simula envio de notificacao (95% sucesso) |
| `NotificationRepository` | Armazena notificacoes em memoria |

## Como Executar

### Requisitos
- Java 21
- Maven 3.8+

### 1. Build do projeto
```bash
mvn clean install
```

### 2. Iniciar Notification Service (primeiro)
```bash
mvn spring-boot:run -pl notification-service
```

### 3. Iniciar Payment Service
Em outro terminal:
```bash
mvn spring-boot:run -pl payment-service
```

### Portas

| Servico | gRPC | HTTP (Actuator) |
|---|---|---|
| Payment Service | `localhost:9090` | `localhost:8080` |
| Notification Service | `localhost:9091` | `localhost:8081` |

## Testando com Postman (gRPC)

O Postman suporta gRPC nativamente. Siga os passos:

### Configuracao inicial (uma vez)

1. Abra o Postman e clique em **New** > **gRPC**
2. Em **Service definition**, clique em **Import .proto file**
3. Importe os arquivos `.proto` da pasta:
   ```
   proto-contracts/src/main/proto/
   ├── common/common.proto
   ├── payment/payment.proto
   └── notification/notification.proto
   ```
4. Nos import paths, adicione: `proto-contracts/src/main/proto/`

### Payment Service - Endpoints (localhost:9090)

#### ProcessPayment (Unario)
- **URL:** `localhost:9090`
- **Method:** `payment.PaymentService/ProcessPayment`
- **Message:**
```json
{
  "customer_id": "CUST001",
  "order_id": "ORD123",
  "amount": {
    "currency": "BRL",
    "amount_cents": 50000
  },
  "method": "PIX",
  "metadata": {
    "ip": "192.168.1.1",
    "device": "mobile"
  }
}
```

#### GetPayment (Unario)
- **URL:** `localhost:9090`
- **Method:** `payment.PaymentService/GetPayment`
- **Message:**
```json
{
  "payment_id": "COPIE_O_ID_DA_RESPOSTA_ANTERIOR"
}
```

#### ListPayments (Server Streaming)
- **URL:** `localhost:9090`
- **Method:** `payment.PaymentService/ListPayments`
- **Message:**
```json
{
  "customer_id": "CUST001"
}
```

#### CancelPayment (Unario)
- **URL:** `localhost:9090`
- **Method:** `payment.PaymentService/CancelPayment`
- **Message:**
```json
{
  "payment_id": "COPIE_O_ID_DA_RESPOSTA",
  "reason": "Desistencia do cliente"
}
```

### Notification Service - Endpoints (localhost:9091)

#### SendNotification (Unario)
- **URL:** `localhost:9091`
- **Method:** `notification.NotificationService/SendNotification`
- **Message:**
```json
{
  "recipient_id": "USER001",
  "channel": "EMAIL",
  "priority": "HIGH",
  "template_id": "payment_approved",
  "template_vars": {
    "payment_id": "PAY-001",
    "amount": "R$ 500,00"
  },
  "email_address": "usuario@exemplo.com"
}
```

#### GetNotification (Unario)
- **URL:** `localhost:9091`
- **Method:** `notification.NotificationService/GetNotification`
- **Message:**
```json
{
  "notification_id": "COPIE_O_ID_DA_RESPOSTA"
}
```

### Alternativa: grpcurl (linha de comando)

```bash
# Listar servicos disponiveis
grpcurl -plaintext localhost:9090 list

# Processar pagamento
grpcurl -plaintext -d '{
  "customer_id": "CUST001",
  "order_id": "ORD123",
  "amount": {"currency": "BRL", "amount_cents": 50000},
  "method": "PIX"
}' localhost:9090 payment.PaymentService/ProcessPayment

# Consultar pagamento
grpcurl -plaintext -d '{"payment_id": "SEU_ID"}' \
  localhost:9090 payment.PaymentService/GetPayment

# Listar pagamentos de um cliente (server streaming)
grpcurl -plaintext -d '{"customer_id": "CUST001"}' \
  localhost:9090 payment.PaymentService/ListPayments

# Cancelar pagamento
grpcurl -plaintext -d '{"payment_id": "SEU_ID", "reason": "teste"}' \
  localhost:9090 payment.PaymentService/CancelPayment

# Enviar notificacao diretamente
grpcurl -plaintext -d '{
  "recipient_id": "USER001",
  "channel": "EMAIL",
  "priority": "HIGH",
  "template_id": "payment_approved",
  "email_address": "user@example.com"
}' localhost:9091 notification.NotificationService/SendNotification
```

## Testes Automatizados

### Executando os testes

```bash
# Todos os testes
mvn test

# Apenas um servico
mvn test -pl payment-service
mvn test -pl notification-service

# Uma classe especifica
mvn test -pl payment-service -Dtest=PaymentGrpcServiceTest

# Um metodo especifico
mvn test -pl payment-service -Dtest=PaymentServiceTest#shouldCancelPendingPayment
```

### Suite de testes (100 testes)

```
 PAYMENT SERVICE (72 testes)
 ______________________________

 PaymentGrpcServiceTest ........... 19 testes
   processPayment: sucesso, validacoes (customerId, orderId, amount, method)
   getPayment: sucesso, not found, internal error
   listPayments: sucesso, vazio, customerId vazio, internal error
   cancelPayment: sucesso, not found, failed precondition, internal error

 PaymentServiceTest ............... 14 testes
   processPayment: approved, rejected (>R$10k), boundary (=R$10k), metadata, metodos
   getPayment: sucesso, not found
   getPaymentsByCustomer: com resultados
   cancelPayment: pending, processing, approved (erro), rejected (erro), cancelled (erro), inexistente

 PaymentNotificationSenderTest .... 9 testes
   notifyCustomer: template approved, template rejected, formato money, auth_code
   fallback: graceful com diferentes excecoes (UNAVAILABLE, timeout, NPE)

 NotificationClientTest ........... 4 testes
   sendNotification: sucesso (servidor in-process real)
   erros: UNAVAILABLE, DEADLINE_EXCEEDED, INTERNAL

 LoggingInterceptorTest ........... 6 testes
   request-id: geracao, preservacao, blank handling
   trailers: request-id propagado, status OK, status erro

 ExceptionHandlerInterceptorTest .. 8 testes
   mapeamento: IllegalArgument->INVALID_ARGUMENT, IllegalState->FAILED_PRECONDITION
   passthrough: StatusRuntimeException, generic->INTERNAL
   seguranca: nao vaza detalhes internos
   error detail: attachment nos trailers

 PaymentMapperTest ................ 5 testes
   toProto, toDomain, todos status, todos metodos, method desconhecido

 PaymentRepositoryTest ............ 7 testes
   CRUD: save, findById, findByCustomerId, findAll, overwrite, imutabilidade


 NOTIFICATION SERVICE (28 testes)
 __________________________________

 NotificationGrpcServiceTest ...... 17 testes
   sendNotification: sucesso, validacoes (recipientId, channel, templateId)
   getNotification: sucesso, not found, internal error
   batchSend: sucesso, falhas, mix sucesso/falha, vazio
   monitorNotifications: single, multiple, not found (bidirecional)
   mapeamento: todos canais, todas prioridades

 NotificationServiceTest .......... 7 testes
   sendNotification: sucesso, deliveredAt, todos canais, todas prioridades, template vars
   getNotification: sucesso, not found

 NotificationRepositoryTest ....... 4 testes
   save, findById, overwrite, null id
```

### O que cada camada de teste cobre

```
 PIRAMIDE DE TESTES
 ___________________

              /\
             /  \
            / In \        NotificationClientTest
           / teg  \       (servidor gRPC in-process real)
          / racao  \
         /----------\
        /            \
       /  Unitarios   \   PaymentServiceTest, PaymentGrpcServiceTest
      /   (Mockito)    \  NotificationServiceTest, MapperTest, etc
     /                  \
    /   100 testes no    \
   /    total passando    \
  /________________________\
```

### Ferramentas utilizadas nos testes

| Ferramenta | Uso |
|---|---|
| **JUnit 5** | Framework de teste (`@Test`, `@BeforeEach`, `@ExtendWith`) |
| **Mockito** | Simula dependencias (`@Mock`, `when()`, `verify()`) |
| **AssertJ** | Assertions fluentes (`assertThat().isEqualTo()`) |
| **gRPC InProcess** | Servidor gRPC em memoria para testar o client real |
| **ArgumentCaptor** | Captura argumentos passados a mocks para inspecao |

## Patterns de Producao Implementados

### Interceptors (transversais)
```
 Request gRPC
     |
     v
 +---------------------------+
 | LoggingInterceptor        |  --> gera request-id, MDC, mede latencia
 +---------------------------+
     |
     v
 +---------------------------+
 | ExceptionHandlerInterceptor| --> captura excecoes, mapeia para Status gRPC
 +---------------------------+
     |
     v
 [PaymentGrpcService]
```

### Resiliencia (comunicacao entre servicos)
```
 PaymentNotificationSender
     |
     |  @CircuitBreaker (notificationService)
     |    sliding-window: 10
     |    failure-rate: 50%
     |    wait-in-open: 10s
     |
     |  @Retry (notificationService)
     |    max-attempts: 3
     |    wait: 500ms
     |
     v
 NotificationClient
     |
     |  deadline: 5s (evita hang indefinido)
     |
     v
 NotificationService (porta 9091)
     |
     X--- FALLBACK: loga erro, nao quebra o pagamento
```

### gRPC Streaming

| Tipo | Metodo | Descricao |
|---|---|---|
| Unario | `ProcessPayment`, `GetPayment`, `CancelPayment`, `SendNotification`, `GetNotification` | Request-response simples |
| Server Streaming | `ListPayments` | Servidor envia multiplas respostas |
| Client Streaming | `BatchSend` | Cliente envia multiplas notificacoes, recebe sumario |
| Bidirecional | `MonitorNotifications` | Cliente e servidor trocam mensagens livremente |

## Endpoints de Monitoramento

| Endpoint | Payment Service | Notification Service |
|---|---|---|
| Health Check | http://localhost:8080/actuator/health | http://localhost:8081/actuator/health |
| Metricas Prometheus | http://localhost:8080/actuator/prometheus | http://localhost:8081/actuator/prometheus |
| Info | http://localhost:8080/actuator/info | http://localhost:8081/actuator/info |

## Estrutura do Projeto

```
grpc-microservices/
├── proto-contracts/                    # Contratos .proto compartilhados
│   └── src/main/proto/
│       ├── common/common.proto         # ResponseMetadata, PageRequest, ErrorDetail
│       ├── payment/payment.proto       # PaymentService (4 RPCs)
│       └── notification/notification.proto # NotificationService (4 RPCs)
│
├── payment-service/                    # Microsservico de pagamentos
│   └── src/
│       ├── main/java/.../payment/
│       │   ├── grpc/PaymentGrpcService.java
│       │   ├── service/PaymentService.java
│       │   ├── service/PaymentNotificationSender.java
│       │   ├── client/NotificationClient.java
│       │   ├── mapper/PaymentMapper.java
│       │   ├── repository/PaymentRepository.java
│       │   ├── domain/Payment.java
│       │   └── interceptor/
│       │       ├── LoggingInterceptor.java
│       │       └── ExceptionHandlerInterceptor.java
│       └── test/java/.../payment/      # 72 testes
│
└── notification-service/               # Microsservico de notificacoes
    └── src/
        ├── main/java/.../notification/
        │   ├── grpc/NotificationGrpcService.java
        │   ├── service/NotificationService.java
        │   ├── repository/NotificationRepository.java
        │   └── domain/Notification.java
        └── test/java/.../notification/ # 28 testes
```

## Stack Tecnica

- **Java 21** + **Spring Boot 3.2.5**
- **gRPC** (io.grpc 1.62.2) + **Protocol Buffers 3**
- **grpc-spring-boot-starter** (net.devh 3.1.0)
- **Resilience4j** (Circuit Breaker + Retry)
- **Micrometer** + **Prometheus** (metricas)
- **Logback** + **Logstash Encoder** (logging estruturado)
- **JUnit 5** + **Mockito** + **AssertJ** (testes)

## Seguranca (para Producao)

Atualmente usando `PLAINTEXT`. Para producao:

```yaml
# 1. Habilitar TLS
grpc:
  server:
    security:
      enabled: true
      certificate-chain: classpath:certs/server.crt
      private-key: classpath:certs/server.key

# 2. mTLS (cliente tambem apresenta certificado)
# 3. JWT via interceptor no Metadata
```

## Evolucao de Contrato

| Permitido (sem quebrar) | Proibido (quebra) |
|---|---|
| Adicionar novos campos | Mudar tipo de campo |
| Adicionar novos RPCs | Mudar repeated para escalar |
| Renomear campos (tag e o que importa) | Reusar tag removido |
| Remover campos (usar `reserved`) | Mudar unario para streaming |

## Proximos Passos

1. **Persistencia real**: PostgreSQL + Spring Data JPA
2. **Service Mesh**: Istio/Linkerd para mTLS e observability
3. **gRPC-Gateway**: Expor REST API a partir dos .proto
4. **Kafka**: Dead Letter Queue para notificacoes falhadas
5. **OpenTelemetry**: Tracing distribuido completo
