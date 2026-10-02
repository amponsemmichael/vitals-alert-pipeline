# Multi-stage Dockerfile for vitals-consumer
# Stage 1 builds the fat JAR; Stage 2 runs it on a minimal JRE image.

# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY vitals-consumer/pom.xml ./vitals-consumer/pom.xml

COPY vitals-consumer/src ./vitals-consumer/src

RUN --mount=type=cache,target=/root/.m2 \
    apk add --no-cache maven && \
    mvn -f vitals-consumer/pom.xml package -DskipTests --no-transfer-progress

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Run as non-root user — container hardening, SPEC.md section 8
RUN addgroup -S vitals && adduser -S vitals -G vitals
USER vitals

WORKDIR /app

COPY --from=build /workspace/vitals-consumer/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
