FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY ./target/gate-control-service-*.jar app.jar

ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]