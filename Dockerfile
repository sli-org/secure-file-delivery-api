# =============================================================================
# Multi-stage Dockerfile for statement-api
# Stage 1: Build with Maven | Stage 2: Slim JRE runtime
# =============================================================================

# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy POM first (layer caching for dependencies)
COPY pom.xml ./
COPY settings.xml* ./
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copy source and build (skip tests — run separately)
COPY src src
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true -B

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR
COPY --from=builder /build/target/*.jar app.jar

# Health check via Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8093/actuator/health || exit 1

EXPOSE 8093

ENTRYPOINT ["java", "-jar", "app.jar"]
