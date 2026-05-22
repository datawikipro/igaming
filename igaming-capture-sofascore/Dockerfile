# ---------- build application ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
WORKDIR /app
COPY igaming-capture-sofascore/pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B
COPY igaming-capture-sofascore/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar capture-sofascore.jar
EXPOSE 3041
ENTRYPOINT ["java", "-jar", "capture-sofascore.jar"]
