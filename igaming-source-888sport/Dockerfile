# --- СТАДИЯ 2: Финальный образ (сборка на хосте) ---
FROM ghcr.io/datawikipro/igaming-source-base:latest
WORKDIR /app

COPY igaming-source-888sport/target/*.jar app.jar

ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright
ENTRYPOINT ["java", "-jar", "app.jar"]
