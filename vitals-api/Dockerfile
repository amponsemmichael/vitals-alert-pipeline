# Multi-stage Dockerfile for vitals-api
# Stage 1 builds the fat JAR; Stage 2 runs it on a minimal JRE image.
# The build stage is never shipped — only the JRE layer reaches the registry.

# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy parent POM first so Maven can resolve the multi-module structure
COPY pom.xml ./pom.xml
COPY vitals-api/pom.xml ./vitals-api/pom.xml

# Download dependencies in a separate layer — only re-runs when pom.xml changes
RUN --mount=type=cache,target=/root/.m2 \
    find . -name "*.java" -delete || true

COPY vitals-api/src ./vitals-api/src

RUN --mount=type=cache,target=/root/.m2 \
    apk add --no-cache maven && \
    mvn -f vitals-api/pom.xml package -DskipTests --no-transfer-progress

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Run as non-root user — container hardening, SPEC.md section 8
RUN addgroup -S vitals && adduser -S vitals -G vitals
USER vitals

WORKDIR /app

COPY --from=build /workspace/vitals-api/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
