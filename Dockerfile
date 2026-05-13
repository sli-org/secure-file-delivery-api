# =============================================================================
# Multi-stage Dockerfile for statement-api
# Stage 1: Build with Maven | Stage 2: Slim JRE runtime
#
# Prerequisites — run once before building this image:
#   cd ../common-api-starter-parent && mvn install -DskipTests -B
#
# Then build & start everything with:
#   docker compose up --build -d
# =============================================================================

# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy POM and custom settings first (maximises Docker layer cache)
COPY pom.xml ./
COPY settings.xml ./

# Seed the Maven local repository with pre-built common-starter-* artifacts.
# These are copied from the host .m2 cache via docker compose (see docker-compose.yml).
# This avoids needing GitHub Packages authentication inside the Docker build.
COPY .m2 /root/.m2

# Download all remaining external dependencies (Maven Central only)
RUN mvn dependency:go-offline -s settings.xml -B 2>/dev/null || true

# Copy source and compile (tests run separately in CI)
COPY src src
RUN mvn clean package -s settings.xml -DskipTests -Dcheckstyle.skip=true -B

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from build stage
COPY --from=builder /build/target/*.jar app.jar

# Health check via Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8093/actuator/health || exit 1

EXPOSE 8093

ENTRYPOINT ["java", "-jar", "app.jar"]
