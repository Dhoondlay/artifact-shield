# ─────────────────────────────────────────────────────────────────────
# Artifact-Shield Docker Image
# Multi-stage build: compile + test → minimal JRE runtime image
# ─────────────────────────────────────────────────────────────────────

# ── Stage 1: Build ────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and POM first (exploits layer cache)
COPY mvnw       ./
COPY .mvn       .mvn/
COPY pom.xml    ./

# Download dependencies (cached if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy source and build production JAR (skip tests here; run in CI)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Non-root user for security hardening
RUN addgroup -S shield && adduser -S shield -G shield
USER shield

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /workspace/target/artifact-shield-*.jar app.jar

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/v1/shield/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
