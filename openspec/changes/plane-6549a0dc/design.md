# Architecture Design: #47: [betboom] Восстановление сбора линии: обход QRATOR WAF (JS-challenge) и стабилизация Playwright/Firefox

## 1. Архитектура интеграции BetBoom
Модуль `igaming-source-betboom` обеспечивает сбор линии и коэффициентов российского букмекера BetBoom:
- `BetboomBrowserClient`: компонент работы со SPA BetBoom на базе браузерного движка Playwright/Firefox. Bypasses QRATOR WAF JS-challenge, извлекает актуальное дерево видов спорта и подписки на матчи через доступ к Redux-состоянию приложения (`SportbookWSApi/tree_ws/v1`).
- `BetboomApiClient`: резервный REST-клиент к эндпоинтам `siteapi.betboom.ru`.
- `MatchService`: сервис оркестрации парсинга событий, маппинга исходов в JPA-сущность `MatchCache` и отправки обновлений в Aggregator API.
- `MatchFetchScheduler`: планировщик периодического запуска сбора Live и Prematch линий в роли `league-crawler`.
- `AbstractBetboomMarketStrategy` и специализированные стратегии (`BetboomMainResultStrategy`, `BetboomTotalStrategy`, `BetboomHandicapStrategy`, `BetboomStatsStrategy`, `BetboomEsportsStrategy`): ООП-обработка рынков котировок (1X2, тоталы, форы, статистика, киберспорт).

## 2. Обход QRATOR WAF и конфигурация браузера
- Защита QRATOR WAF на `betboom.ru` выполняет активный фингерпринтинг браузера (Canvas, WebGL, CDP-инжекции). Использование стандартного Chromium CDP блокируется на этапе JS-challenge.
- Использование Firefox (`app.browser.engine=firefox`, stealth profile `HEADLESS_STEALTH` / `XVFB_HEADED`) успешно решает JS-challenge QRATOR WAF и загружает SPA.
- Базовый OCI-образ переключен на `100.78.183.101:30500/igaming-source-base:latest`, содержащий все необходимые системные зависимости (`libgtk-3-0`, `libxcb-shm0`, `libasound2` и др.) и предустановленные браузеры в `/ms-playwright`.
- Автоматическое конфигурирование `PLAYWRIGHT_BROWSERS_PATH=/ms-playwright` в Jib и `BrowserService`.

## 3. Стабилизация и защита от OOM / утечек памяти
- Инкрементальное сохранение: сбор событий из Redux Store сохраняется в базу данных `match_cache` батчами после каждого обработанного вида спорта (`batchConsumer`), гарантируя непрерывное наполнение БД и устойчивость к прерываниям.
- Синхронизация сессий краулера: `fetchLiveData` и `fetchPrematchData` используют мьютекс `crawlerLock`, предотвращая параллельное открытие конкурирующих вкладок браузера и перерасход оперативной памяти.
- Оптимизация кликов: установка таймаута на клик по селекторам видов спорта исключает зависание на скрытых или перекрытых элементах.
