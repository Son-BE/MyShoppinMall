FROM amazoncorretto:17
COPY build/libs/weather-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
CMD java $JAVA_OPTS -jar /app.jar
