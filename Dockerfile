FROM openjdk:17-jdk-alpine
COPY build/libs/weather-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
