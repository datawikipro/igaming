# ---------- build application ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
WORKDIR /app
COPY igaming-capture-injuries/pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B
COPY igaming-capture-injuries/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar capture-injuries.jar
EXPOSE 3044
ENTRYPOINT ["java", "-jar", "capture-injuries.jar"]
