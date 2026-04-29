# Exemplos de Chamadas gRPC

## Requisitos

Instalar grpcurl:
```bash
# macOS
brew install grpcurl

# Linux
curl -sSL "https://github.com/fullstorydev/grpcurl/releases/download/v1.8.9/grpcurl_1.8.9_linux_x86_64.tar.gz" | tar -xz -C /usr/local/bin

# Windows (Chocolatey)
choco install grpcurl
```

## Payment Service (porta 9090)

### 1. Listar serviços disponíveis

```bash
grpcurl -plaintext localhost:9090 list
```

Output esperado:
```
grpc.health.v1.Health
grpc.reflection.v1alpha.ServerReflection
payment.PaymentService
```

### 2. Descrever serviço

```bash
grpcurl -plaintext localhost:9090 describe payment.PaymentService
```

### 3. Processar Pagamento - PIX (aprovado)

```bash
grpcurl -plaintext -d '{
  "customer_id": "CUST001",
  "order_id": "ORD123",
  "amount": {
    "currency": "BRL",
    "amount_cents": 50000
  },
  "method": "PIX",
  "metadata": {
    "ip": "192.168.1.1",
    "device": "mobile",
    "user_agent": "iOS/16.0"
  }
}' localhost:9090 payment.PaymentService/ProcessPayment
```

Resposta esperada:
```json
{
  "payment_id": "abc123...",
  "status": "APPROVED",
  "authorization_code": "A1B2C3D4",
  "created_at": "2024-04-22T10:15:30Z",
  "metadata": {
    "request_id": "req-xyz...",
    "timestamp": "2024-04-22T10:15:30Z",
    "service_version": "1.0.0"
  }
}
```

### 4. Processar Pagamento - Cartão de Crédito (valor alto - rejeitado)

```bash
grpcurl -plaintext -d '{
  "customer_id": "CUST002",
  "order_id": "ORD456",
  "amount": {
    "currency": "BRL",
    "amount_cents": 1500000
  },
  "method": "CREDIT_CARD"
}' localhost:9090 payment.PaymentService/ProcessPayment
```

Resposta esperada (rejeitado porque > R$ 10.000):
```json
{
  "payment_id": "def456...",
  "status": "REJECTED",
  "authorization_code": "E5F6G7H8",
  ...
}
```

### 5. Consultar Pagamento

```bash
# Substitua PAYMENT_ID pelo ID retornado acima
grpcurl -plaintext -d '{
  "payment_id": "abc123..."
}' localhost:9090 payment.PaymentService/GetPayment
```

### 6. Cancelar Pagamento

```bash
grpcurl -plaintext -d '{
  "payment_id": "abc123...",
  "reason": "Customer request"
}' localhost:9090 payment.PaymentService/CancelPayment
```

### 7. Listar Pagamentos (Server Streaming)

```bash
grpcurl -plaintext -d '{
  "customer_id": "CUST001",
  "page": {
    "page": 0,
    "size": 10
  }
}' localhost:9090 payment.PaymentService/ListPayments
```

## Notification Service (porta 9091)

### 1. Enviar Notificação por Email

```bash
grpcurl -plaintext -d '{
  "recipient_id": "CUST001",
  "channel": "EMAIL",
  "priority": "HIGH",
  "template_id": "payment_approved",
  "template_vars": {
    "payment_id": "abc123",
    "amount": "BRL 500.00",
    "auth_code": "A1B2C3D4"
  },
  "email_address": "customer@example.com"
}' localhost:9091 notification.NotificationService/SendNotification
```

### 2. Enviar Notificação por SMS

```bash
grpcurl -plaintext -d '{
  "recipient_id": "CUST002",
  "channel": "SMS",
  "priority": "URGENT",
  "template_id": "payment_rejected",
  "template_vars": {
    "payment_id": "def456",
    "reason": "Insufficient funds"
  },
  "phone_number": "+5511999999999"
}' localhost:9091 notification.NotificationService/SendNotification
```

### 3. Consultar Status da Notificação

```bash
grpcurl -plaintext -d '{
  "notification_id": "notif-xyz..."
}' localhost:9091 notification.NotificationService/GetNotification
```

## Testes de Erro

### 1. Customer ID vazio (deve retornar INVALID_ARGUMENT)

```bash
grpcurl -plaintext -d '{
  "customer_id": "",
  "order_id": "ORD999",
  "amount": {
    "currency": "BRL",
    "amount_cents": 10000
  },
  "method": "PIX"
}' localhost:9090 payment.PaymentService/ProcessPayment
```

Erro esperado:
```
ERROR:
  Code: InvalidArgument
  Message: customer_id is required
```

### 2. Valor zero (deve retornar INVALID_ARGUMENT)

```bash
grpcurl -plaintext -d '{
  "customer_id": "CUST001",
  "order_id": "ORD999",
  "amount": {
    "currency": "BRL",
    "amount_cents": 0
  },
  "method": "PIX"
}' localhost:9090 payment.PaymentService/ProcessPayment
```

### 3. Payment ID inexistente (deve retornar NOT_FOUND)

```bash
grpcurl -plaintext -d '{
  "payment_id": "INVALID_ID"
}' localhost:9090 payment.PaymentService/GetPayment
```

Erro esperado:
```
ERROR:
  Code: NotFound
  Message: Payment not found: INVALID_ID
```

## Health Checks

### gRPC Health Check

```bash
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check
grpcurl -plaintext localhost:9091 grpc.health.v1.Health/Check
```

### HTTP Health Check (Actuator)

```bash
curl http://localhost:8080/actuator/health | jq '.'
curl http://localhost:8081/actuator/health | jq '.'
```

## Métricas Prometheus

```bash
# Payment Service
curl http://localhost:8080/actuator/prometheus | grep grpc_server

# Notification Service  
curl http://localhost:8081/actuator/prometheus | grep grpc_server
```

## Streaming Avançado (Client Streaming)

Para testar client streaming, seria necessário usar código ou ferramenta que suporte streaming.
Exemplo conceitual:

```bash
# Não suportado diretamente pelo grpcurl
# Use o client Java ou BloomRPC para testar
```

## Debugging

### Ver estrutura do serviço completo

```bash
grpcurl -plaintext localhost:9090 describe payment.PaymentService.ProcessPayment
```

### Ver estrutura de uma mensagem

```bash
grpcurl -plaintext localhost:9090 describe payment.PaymentRequest
```

### Request com headers customizados

```bash
grpcurl -plaintext \
  -H 'request-id: custom-req-123' \
  -d '{"customer_id":"CUST001",...}' \
  localhost:9090 payment.PaymentService/ProcessPayment
```
