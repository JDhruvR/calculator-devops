# ─── Stage 1: Build ───────────────────────────────────────────────────────────
# Use Maven with JDK 21 to compile and package the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml first and download dependencies as a separate cached layer.
# This layer is only invalidated when pom.xml changes, not on every src change.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source and build the JAR (tests are run in CI, not during image build)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Use a slim JRE-only image — no JDK, no Maven, no build tools
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security (never run as root in production)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the compiled JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Transfer ownership to non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Document the port the application listens on
EXPOSE 8080

# Health check — Docker will mark container unhealthy if this fails
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
