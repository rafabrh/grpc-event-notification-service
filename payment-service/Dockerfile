FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR gerado
COPY payment-service/target/payment-service-*.jar app.jar

# Criar usuário não-root
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expor portas
EXPOSE 8080 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Rodar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
