# Proposal: #82: [marathonbet-by] Подключить Playwright volume mount и восстановить сбор котировок

## Context
Plane Task ID: `5d6f2860-08fe-4213-b497-8a40fb553808`

## Description
<<<<<<< HEAD
Краулер белорусского букмекера Marathonbet (`igaming-source-marathonbet-by-crawler`) сталкивается с блокировкой прямого HTTP-доступа к эндпоинтам меню (`https://www.marathonbet.by/su/react/event/menu/prematch` и `live`), из-за чего сервис активирует автоматический fallback на `BrowserService` (Playwright Chromium, профиль `HEADLESS_STEALTH` / `XVFB_HEADED`).

Однако запуск браузера завершался критической ошибкой:
```
Error: Executable doesn't exist at /root/.cache/ms-playwright/chromium-1105/chrome-linux/chrome
```
Причиной является отсутствие монтирования хостового кэша браузеров `/data/playwright-browsers` в Pod краулера и лоадера `marathonbet-by`, а также отсутствие необходимых переменных окружения `PLAYWRIGHT_BROWSERS_PATH` и `PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS`. В результате в базе данных `igaming_marathonbet_by` линия матчей опустела (`match_cache = 0`).

Данное предложение предусматривает:
1. Подключение volume mount для кэша браузеров Playwright (`hostPath: /data/playwright-browsers` -> `/root/.cache/ms-playwright`) и оперативной памяти (`/dev/shm`, `/tmp`) для компонентов `igaming-source-marathonbet-by-crawler` и `igaming-source-marathonbet-by-loader` в `igaming-k8s/marathonbet.by.yaml`.
2. Конфигурирование переменных окружения `PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright` и `PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true`.
3. Добавление Actuator-проб (`startupProbe`, `livenessProbe`, `readinessProbe`) для краулера и выставление `priorityClassName: prod-critical`.
4. Применение обновленного манифеста в Kubernetes, запуск 5-минутного soak-теста и восстановление сбора котировок в базу данных `match_cache` (критерий >= 500 матчей).
=======

>>>>>>> feature/plane-456dd438
