FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy pre-built JAR from local build (mvnw clean package)
COPY target/quarkus-app/lib/ ./lib/
COPY target/quarkus-app/*.jar ./
COPY target/quarkus-app/app/ ./app/
COPY target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8089 8443

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]

USER nobody
