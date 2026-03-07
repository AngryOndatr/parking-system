FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8761

ENV SERVER_ADDRESS=0.0.0.0

ENTRYPOINT ["java", "-jar", "app.jar"]
