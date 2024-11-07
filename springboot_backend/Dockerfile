FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/springboot_backend-0.0.1-SNAPSHOT.jar /app/ace_backend.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "ace_backend.jar"]