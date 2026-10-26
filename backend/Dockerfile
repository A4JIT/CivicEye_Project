# ===================================================
# Stage 1: Build JAR using Maven with Eclipse Temurin JDK 17
# ===================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================================================
# Stage 2: Minimal Production JRE 17 Runtime
# ===================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Run as non-root user for cloud security best practices
RUN addgroup -S civiceye && adduser -S civiceye -G civiceye

# Create uploads storage folder and grant permissions
RUN mkdir -p /app/uploads && chown -R civiceye:civiceye /app

USER civiceye

# Copy jar from builder
COPY --from=builder --chown=civiceye:civiceye /build/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Production JVM flags for memory optimization in cloud containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
