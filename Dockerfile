# Stage 1: Build
FROM maven:3.9-amazoncorretto-21-alpine AS builder
WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code
COPY src src

# Build the JAR
RUN mvn clean package -Dmaven.test.skip=true -q

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
USER nobody
ENTRYPOINT ["java", "-jar", "app.jar"]
