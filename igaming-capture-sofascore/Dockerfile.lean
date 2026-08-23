FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/igaming-capture-sofascore-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3041
ENTRYPOINT ["java", "-jar", "app.jar"]
