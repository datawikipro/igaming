# --- СТАДИЯ 1: Сборка JAR внутри Docker ---
FROM maven:3.9-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Копируем весь проект
COPY . .

# Собираем с использованием кэша зависимостей
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -pl igaming-source-caesars -am -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -Dmaven.compiler.release=17

# Гарантируем установку бинарников Chromium Playwright
RUN mvn -pl igaming-source-caesars exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"

# --- СТАДИЯ 2: Финальный образ ---
FROM ghcr.io/datawikipro/igaming-source-base:latest
WORKDIR /app

COPY --from=build /app/igaming-source-caesars/target/*.jar app.jar
COPY --from=build /root/.cache/ms-playwright /root/.cache/ms-playwright

ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright
ENTRYPOINT ["java", "-jar", "app.jar"]
