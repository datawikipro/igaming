# Proposal: #47: [betboom] Восстановление сбора линии: обход QRATOR WAF (JS-challenge) и стабилизация Playwright/Firefox

## Context
Plane Task ID: `6549a0dc-22b7-436f-8e03-0aff21c08100`

## Description
Краулер букмекера BetBoom (`igaming-source-betboom`) сталкивается с блокировкой со стороны антибот-защиты QRATOR WAF (JS-challenge) при прямых HTTP-запросах и при использовании Chromium CDP.
Для успешного прохождения проверки используется Playwright с браузерным движком Firefox (`app.browser.engine=firefox`, `HEADLESS_STEALTH`).
Однако контейнер краулера собирался на базе `eclipse-temurin:21-jre`, в котором отсутствуют необходимые системные X11/GTK библиотеки для запуска Firefox (`libxcb-shm0`, `libgtk-3-0`, `libasound2` и др.), что вызывало сбой при вызове `playwright.firefox().launch(...)` и приводило к нулевому количеству собранных матчей (`match_cache = 0`).

Данное предложение предусматривает:
1. Перевод Jib-сборки модуля `igaming-source-betboom` на базовый образ `100.78.183.101:30500/igaming-source-base:latest`, содержащий все необходимые системные зависимости и предустановленные браузеры Playwright в `/ms-playwright`.
2. Конфигурирование переменной окружения `PLAYWRIGHT_BROWSERS_PATH=/ms-playwright` в сборке контейнера и автоматический fallback в `BrowserService`.
3. Устранение ошибки компиляции `@Override` на методе `resolveScope` в `AbstractBetboomMarketStrategy`.
4. Сборку, деплой в K8s, стабилизацию работы Firefox и достижение критерия наполнения линии (`match_cache >= 500`).
