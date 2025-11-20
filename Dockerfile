FROM maven:3.9-amazoncorretto-21-alpine AS builder
WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B \
    -Dmaven.artifact.threads=50 \
    -Dhttp.maxConnections=50

# Copy source code and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -Dmaven.test.skip=true \
    -Dmaven.javadoc.skip=true \
    -o -T 1C

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

USER nobody
