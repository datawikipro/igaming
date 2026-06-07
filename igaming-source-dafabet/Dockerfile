# --- СТАДИЯ 1: Сборка JAR внутри Docker ---
FROM maven:3.9-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Копируем весь проект
COPY . .

# Собираем с использованием кэша зависимостей
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -pl igaming-source-dafabet -am

# --- СТАДИЯ 2: Финальный образ ---
FROM ghcr.io/datawikipro/igaming-source-base:latest
WORKDIR /app

COPY --from=build /app/igaming-source-dafabet/target/*.jar app.jar

ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright
ENTRYPOINT ["java", "-jar", "app.jar"]
