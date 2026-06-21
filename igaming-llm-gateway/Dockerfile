# ---------- build application ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
WORKDIR /app
COPY igaming-llm-gateway/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY igaming-llm-gateway/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar gateway.jar
EXPOSE 3040
ENTRYPOINT ["java", "-jar", "gateway.jar"]
