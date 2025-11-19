FROM maven:3.9-amazoncorretto-21-alpine AS builder
WORKDIR /app

# Copy everything and download only runtime dependencies (no sources, no javadoc)
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -Dmaven.test.skip=true \
    -Dmaven.artifact.threads=20 \
    -Ddownload.plugin.skip.sources=true \
    -Ddownload.plugin.skip.javadoc=true \
    -Dmaven.javadoc.skip=true \
    -T 1C

FROM amazoncorretto:21-alpine-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

USER nobody