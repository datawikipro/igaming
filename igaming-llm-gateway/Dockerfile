# ---------- build igaming-dto (shared dependency) ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build-dto
WORKDIR /dto
COPY igaming-dto/pom.xml .
COPY igaming-dto/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn install -DskipTests -B

# ---------- build application ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
COPY --from=build-dto /root/.m2/repository/pro/datawiki /root/.m2/repository/pro/datawiki
WORKDIR /app
COPY igaming-llm-gateway/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY igaming-llm-gateway/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gateway.jar"]
