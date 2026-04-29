# Quick Start - gRPC Microservices

## Inicio Rapido (5 minutos)

### 1. Pre-requisitos

- **Java 21+** instalado
- **Maven 3.8+**
- **grpcurl** (opcional, para testes)

```bash
# Verificar versoes
java -version
mvn --version
```

### 2. Build do Projeto

```bash
cd grpc-microservices
mvn clean install
```

Isso ira:
- Compilar os `.proto` e gerar codigo Java
- Compilar todos os modulos
- Rodar testes unitarios
- Gerar JARs

### 3. Executar os Servicos

**Terminal 1 - Notification Service:**
```bash
mvn spring-boot:run -pl notification-service
```

Aguarde aparecer: `Started NotificationServiceApplication in X seconds`

**Terminal 2 - Payment Service:**
```bash
mvn spring-boot:run -pl payment-service
```

Aguarde aparecer: `Started PaymentServiceApplication in X seconds`

### 4. Testar

**Opcao A - Chamada manual:**
```bash
grpcurl -plaintext -d '{
  "customer_id": "CUST001",
  "order_id": "ORD123",
  "amount": {"currency": "BRL", "amount_cents": 50000},
  "method": "PIX"
}' localhost:9090 payment.PaymentService/ProcessPayment
```

**Opcao B - Health check HTTP:**
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

### 5. Ver Logs

Os logs mostrarao:
- Request ID para correlacao
- Latencia de cada chamada
- Status da notificacao enviada
- Circuit breaker em acao

Exemplo de log esperado:
```
2024-04-22 10:15:30 - request-id=abc123 grpc-method=ProcessPayment gRPC request started
2024-04-22 10:15:30 - Processing payment: customerId=CUST001, orderId=ORD123
2024-04-22 10:15:30 - Calling NotificationService.SendNotification
2024-04-22 10:15:31 - Notification sent: notificationId=xyz789, status=SENT
2024-04-22 10:15:31 - gRPC request completed: latency=1050ms, status=OK
```

---

## Estrutura de Arquivos Importantes

```
grpc-microservices/
├── README.md                    <- Documentacao completa
├── BEST_PRACTICES.md            <- Padroes e boas praticas
├── GRPC_EXAMPLES.md             <- Exemplos de chamadas gRPC
│
├── proto-contracts/             <- Contratos .proto
│   └── src/main/proto/
│       ├── common/common.proto         <- Tipos reutilizaveis
│       ├── payment/payment.proto       <- APIs do Payment Service
│       └── notification/notification.proto  <- APIs do Notification
│
├── payment-service/             <- Microsservico de Pagamentos
│   ├── src/main/java/com/example/payment/
│   │   ├── grpc/PaymentGrpcService.java     <- Implementacao gRPC
│   │   ├── service/PaymentService.java      <- Logica de negocio
│   │   ├── client/NotificationClient.java   <- Client gRPC
│   │   ├── interceptor/                     <- Logging, errors
│   │   └── mapper/PaymentMapper.java        <- Proto <-> Domain
│   └── src/main/resources/application.yml   <- Config
│
└── notification-service/        <- Microsservico de Notificacoes
    ├── src/main/java/com/example/notification/
    │   ├── grpc/NotificationGrpcService.java  <- Impl com streaming
    │   └── service/NotificationService.java   <- Logica de negocio
    └── src/main/resources/application.yml     <- Config
```

---

## O que este projeto demonstra

### Fundamentos
- Definicao de contratos `.proto`
- Geracao de codigo a partir dos `.proto`
- Implementacao de gRPC server (stubs)
- Chamadas gRPC client
- Comunicacao entre microsservicos

### Padroes de Comunicacao
- **Unario**: ProcessPayment, SendNotification
- **Server Streaming**: ListPayments
- **Client Streaming**: BatchSend
- **Bidirecional**: MonitorNotifications

### Producao-Ready
- **Interceptors** (logging, error handling)
- **Circuit Breaker** + Retry (Resilience4j)
- **Deadlines** explicitos
- **Error handling** com status codes corretos
- **Observability** (Prometheus, Actuator)
- **Logging estruturado** (MDC, request-id)
- **Health checks** (gRPC + HTTP)

### Boas Praticas
- Contratos em modulo separado
- Validacao de input
- Mapeamento proto <-> domain
- Testes unitarios com Mockito + JUnit 5
- Documentacao completa

---

## Executar com Docker

```bash
# Build das imagens
mvn clean package
docker-compose build

# Subir os servicos
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar
docker-compose down
```

---

## Testes

```bash
# Testes unitarios (100 testes)
mvn test

# Testes de um modulo especifico
mvn test -pl payment-service
mvn test -pl notification-service

# Teste especifico
mvn test -pl payment-service -Dtest=PaymentGrpcServiceTest
```

---

## Monitoramento

- **Payment Service Health:** http://localhost:8080/actuator/health
- **Notification Service Health:** http://localhost:8081/actuator/health
- **Prometheus Metrics:** http://localhost:8080/actuator/prometheus

---

## Troubleshooting

### Problema: "Port already in use"

```bash
# Windows
netstat -ano | findstr :9090
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :9090
kill -9 <PID>
```

### Problema: Build falha em proto-contracts

```bash
# Limpar e rebuild
mvn clean install -U
```

### Problema: NotificationService nao conecta

Verificar se esta rodando:
```bash
curl http://localhost:8081/actuator/health
```

Ver configuracao de client em `payment-service/src/main/resources/application.yml`:
```yaml
grpc:
  client:
    notification-service:
      address: 'static://localhost:9091'  # Deve apontar pro host correto
```

---

## Proximos Passos

1. **Ler BEST_PRACTICES.md** - Entender padroes de producao
2. **Ler GRPC_EXAMPLES.md** - Ver exemplos de chamadas
3. **Modificar os .proto** - Praticar evolucao de contrato
4. **Adicionar persistencia** - Substituir repository in-memory por JPA
5. **Integrar com Kafka** - DLQ para notificacoes falhadas
6. **Adicionar autenticacao** - JWT validation no interceptor

---

## Recursos de Aprendizado

- [gRPC Oficial](https://grpc.io/docs/languages/java/quickstart/)
- [Protocol Buffers Guide](https://protobuf.dev/programming-guides/proto3/)
- [Spring Boot gRPC](https://yidongnan.github.io/grpc-spring-boot-starter/)
- [Resilience4j](https://resilience4j.readme.io/docs/circuitbreaker)
