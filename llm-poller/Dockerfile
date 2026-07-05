# ---------- build accounts-domain (shared dependency) ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build-domain
WORKDIR /domain
COPY accounts-domain/pom.xml .
COPY accounts-domain/src ./src
RUN mvn install -DskipTests -B

# ---------- build application ----------
FROM ghcr.io/datawikipro/igaming-build-base:latest AS build
WORKDIR /app
COPY --from=build-domain /root/.m2/repository/pro/datawiki /tmp/pro/datawiki
COPY llm-poller/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mkdir -p /root/.m2/repository/pro/datawiki && cp -r /tmp/pro/datawiki/* /root/.m2/repository/pro/datawiki/ && mvn dependency:go-offline -B
COPY llm-poller/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
