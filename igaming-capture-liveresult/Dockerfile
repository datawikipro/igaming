# ---------- build application ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
WORKDIR /app
COPY igaming-capture-liveresult/pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B
COPY igaming-capture-liveresult/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar capture-liveresult.jar
EXPOSE 3042
ENTRYPOINT ["java", "-jar", "capture-liveresult.jar"]
