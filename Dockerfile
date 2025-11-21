# Stage 1: Build
FROM amazoncorretto:21-alpine AS builder
WORKDIR /build

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -q

# Copy source code
COPY src src

# Build the JAR
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
USER nobody
ENTRYPOINT ["java", "-jar", "app.jar"]
