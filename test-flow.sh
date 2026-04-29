#!/bin/bash

# Script de teste completo do fluxo gRPC
# Requer grpcurl instalado: https://github.com/fullstorydev/grpcurl

set -e

PAYMENT_HOST="localhost:9090"
NOTIFICATION_HOST="localhost:9091"

echo "=========================================="
echo "gRPC Microservices - Teste Completo"
echo "=========================================="
echo ""

# 1. Health check dos serviços
echo "1. Verificando saúde dos serviços..."
echo ""

echo "Payment Service:"
curl -s http://localhost:8080/actuator/health | jq '.'
echo ""

echo "Notification Service:"
curl -s http://localhost:8081/actuator/health | jq '.'
echo ""

# 2. Listar serviços disponíveis
echo "2. Listando serviços gRPC disponíveis..."
echo ""

echo "Payment Service:"
grpcurl -plaintext $PAYMENT_HOST list
echo ""

echo "Notification Service:"
grpcurl -plaintext $NOTIFICATION_HOST list
echo ""

# 3. Processar pagamento (valor baixo - aprovado)
echo "3. Processando pagamento APROVADO (R$ 500,00)..."
echo ""

PAYMENT_RESPONSE=$(grpcurl -plaintext -d '{
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
}' $PAYMENT_HOST payment.PaymentService/ProcessPayment)

echo "$PAYMENT_RESPONSE" | jq '.'
PAYMENT_ID=$(echo "$PAYMENT_RESPONSE" | jq -r '.payment_id')
echo ""
echo "Payment ID gerado: $PAYMENT_ID"
echo ""

# 4. Consultar pagamento criado
echo "4. Consultando pagamento criado..."
echo ""

grpcurl -plaintext -d "{
  \"payment_id\": \"$PAYMENT_ID\"
}" $PAYMENT_HOST payment.PaymentService/GetPayment | jq '.'
echo ""

# 5. Processar pagamento (valor alto - rejeitado)
echo "5. Processando pagamento REJEITADO (R$ 15.000,00)..."
echo ""

grpcurl -plaintext -d '{
  "customer_id": "CUST002",
  "order_id": "ORD456",
  "amount": {
    "currency": "BRL",
    "amount_cents": 1500000
  },
  "method": "CREDIT_CARD"
}' $PAYMENT_HOST payment.PaymentService/ProcessPayment | jq '.'
echo ""

# 6. Testar envio direto de notificação
echo "6. Testando envio direto de notificação..."
echo ""

NOTIF_RESPONSE=$(grpcurl -plaintext -d '{
  "recipient_id": "CUST003",
  "channel": "EMAIL",
  "priority": "HIGH",
  "template_id": "test_notification",
  "template_vars": {
    "message": "Teste manual"
  },
  "email_address": "test@example.com"
}' $NOTIFICATION_HOST notification.NotificationService/SendNotification)

echo "$NOTIF_RESPONSE" | jq '.'
NOTIF_ID=$(echo "$NOTIF_RESPONSE" | jq -r '.notification_id')
echo ""
echo "Notification ID gerado: $NOTIF_ID"
echo ""

# 7. Consultar notificação
echo "7. Consultando notificação criada..."
echo ""

grpcurl -plaintext -d "{
  \"notification_id\": \"$NOTIF_ID\"
}" $NOTIFICATION_HOST notification.NotificationService/GetNotification | jq '.'
echo ""

# 8. Testar validação de erro
echo "8. Testando validação (customer_id vazio - deve falhar)..."
echo ""

grpcurl -plaintext -d '{
  "customer_id": "",
  "order_id": "ORD789",
  "amount": {
    "currency": "BRL",
    "amount_cents": 10000
  },
  "method": "PIX"
}' $PAYMENT_HOST payment.PaymentService/ProcessPayment 2>&1 || true
echo ""

# 9. Métricas Prometheus
echo "9. Coletando métricas Prometheus..."
echo ""

echo "Payment Service - Total de chamadas gRPC:"
curl -s http://localhost:8080/actuator/prometheus | grep 'grpc_server_calls_total' | head -5
echo ""

echo "Notification Service - Total de chamadas gRPC:"
curl -s http://localhost:8081/actuator/prometheus | grep 'grpc_server_calls_total' | head -5
echo ""

echo "=========================================="
echo "Teste completo finalizado com sucesso!"
echo "=========================================="
