FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy pre-built JAR (build locally on Mac for speed)
COPY target/*.jar app.jar

EXPOSE 8080
USER nobody
ENTRYPOINT ["java", "-jar", "app.jar"]
