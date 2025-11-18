FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.18
WORKDIR /app

COPY --from=build /build/target/quarkus-app/lib/ /app/lib/
COPY --from=build /build/target/quarkus-app/*.jar /app/
COPY --from=build /build/target/quarkus-app/app/ /app/app/
COPY --from=build /build/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8089 8443

ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]

USER nobody