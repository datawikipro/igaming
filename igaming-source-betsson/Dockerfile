# --- СТАДИЯ 2: Финальный образ (сборка на хосте) ---
FROM ghcr.io/datawikipro/igaming-source-base:latest
WORKDIR /app

COPY igaming-source-betsson/target/*.jar app.jar

ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright
ENTRYPOINT ["java", "-jar", "app.jar"]
