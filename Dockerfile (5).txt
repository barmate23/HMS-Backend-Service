# syntax=docker/dockerfile:1

# ======================
# Build stage
# ======================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src src
RUN mvn clean package -DskipTests

# ======================
# Run stage
# ======================
FROM eclipse-temurin:21-jdk-jammy as runtime
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose Eureka default port
EXPOSE 8084

# ✅ Fix for CgroupInfo NPE in Docker (metrics auto-config disabled)
ENTRYPOINT java \
  -Dspring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration \
  -jar app.jar
