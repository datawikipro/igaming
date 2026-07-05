# ---------- build ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
WORKDIR /app

# 1. Build and install shared libraries in cache mount
COPY igaming-dto /app/igaming-dto
RUN --mount=type=cache,target=/root/.m2 mvn -f igaming-dto/pom.xml install -DskipTests -B

# 2. Resolve microservice dependencies
COPY igaming-infra-operator/pom.xml /app/igaming-infra-operator/
RUN --mount=type=cache,target=/root/.m2 mvn -f igaming-infra-operator/pom.xml dependency:go-offline -B

# 3. Build microservice
COPY igaming-infra-operator/src /app/igaming-infra-operator/src
RUN --mount=type=cache,target=/root/.m2 mvn -f igaming-infra-operator/pom.xml clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/igaming-infra-operator/target/*.jar app.jar
EXPOSE 3041
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
