FROM maven:3.9-amazoncorretto-21-alpine AS builder
WORKDIR /app

# Copy everything and download only runtime dependencies (no sources, no javadoc)
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests \
    -Dmaven.artifact.threads=20 \
    -Ddownload.plugin.skip.sources=true \
    -Ddownload.plugin.skip.javadoc=true \
    -Dmaven.javadoc.skip=true \
    -T 1C

FROM amazoncorretto:21-alpine-jre
WORKDIR /app
COPY --from=builder /app/target/quarkus-app/lib/ ./lib/
COPY --from=builder /app/target/quarkus-app/*.jar ./
COPY --from=builder /app/target/quarkus-app/app/ ./app/
COPY --from=builder /app/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8089 8443

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]

USER nobody
