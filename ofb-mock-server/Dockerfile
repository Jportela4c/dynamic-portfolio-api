FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml and download dependencies (uses persistent cache, parallel downloads)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B \
    -Dmaven.artifact.threads=20 \
    -T 1C

# Copy source and build (uses persistent cache, parallel build)
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests \
    -Dmaven.artifact.threads=20 \
    -T 1C

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/quarkus-app/lib/ ./lib/
COPY --from=builder /app/target/quarkus-app/*.jar ./
COPY --from=builder /app/target/quarkus-app/app/ ./app/
COPY --from=builder /app/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8089 8443

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]

USER nobody
