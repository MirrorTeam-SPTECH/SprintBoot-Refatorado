# ================================
# Dockerfile - Backend Spring Boot
# ================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar arquivos de configuração Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download das dependências (cache layer)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src src

# Build da aplicação
RUN ./mvnw package -DskipTests

# ================================
# Runtime Stage
# ================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar JAR do builder
COPY --from=builder /app/target/*.jar app.jar

# Mudar ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variáveis de ambiente padrão
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

# Entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
