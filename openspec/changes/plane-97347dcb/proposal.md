# Proposal: [crawlers-playwright] Встроить установку Chromium Playwright в Docker-образы браузерных краулеров

## Context
Plane Task ID: `97347dcb-132e-4fdb-9f96-278bf5474324`
Target Module: `igaming-source-winline`

## Description
**Проблема:** Браузерные краулеры `winline-crawler`, `bwin-crawler`, `bettery-crawler`, `betmgm-crawler`, `caesars-crawler`, `betano` падают с ошибкой `Executable doesn't exist at /root/.cache/ms-playwright/chromium-1124/chrome-linux/chrome`. Из-за этого live-линия Winline и Bwin не собирается.

**Что сделать:**

- В Dockerfile или шаги инициализации образов краулеров добавить запуск `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"` или гарантировать наличие бинарников в образе.
