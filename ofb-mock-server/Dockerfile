# Stage 1: Build
FROM maven:3.9-amazoncorretto-21-alpine AS builder
WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code and OpenAPI specs
COPY src src

# Build the Quarkus app
RUN mvn clean package -Dmaven.test.skip=true -q

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy Quarkus app from builder stage
COPY --from=builder /build/target/quarkus-app/lib/ ./lib/
COPY --from=builder /build/target/quarkus-app/*.jar ./
COPY --from=builder /build/target/quarkus-app/app/ ./app/
COPY --from=builder /build/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8089 8443

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]

USER nobody
