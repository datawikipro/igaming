# Implementation Tasks: [UI-CRAWLER-OPS] Ingestion Pipeline Dashboard & Thresholds Monitor

- [x] 1. Изучить текущую архитектуру пайплайна и подготовить структуру OpenSpec
  - [x] 1.1 Анализ текущих модулей `igaming-analytics-service`, `aggregator-api`, `igaming-dto` и эндпоинтов диагностики
  - [x] 1.2 Создание артефактов OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, дельта-спецификация `specs/crawler-engine/spec.md`)
  - [x] 1.3 Декомпозиция чеклиста `tasks.md` для реализации фичи
- [x] 2. Реализовать бэкенд контроллер Crawler Ops & Ingestion Pipeline в `igaming-analytics-service`
  - [x] 2.1 Создание `CrawlerOpsController` с эндпоинтами `/api/v1/crawler-ops/pipeline/stats`, `/api/v1/crawler-ops/thresholds`, `/api/v1/crawler-ops/fleet`, `/api/v1/crawler-ops/fleet/refresh`, `/api/v1/crawler-ops/crawlers/{id}/probe`
  - [x] 2.2 Реализация расчёта соответствия Golden Rule #8 (порог >= 500 матчей), прогресса, статусов `COMPLIANT`, `DEGRADED`, `DEFECT` и свежести данных
  - [x] 2.3 Обновление `MdmUiController` для поддержки маршрутов `/crawler-ops`, `/crawler-ops/`, `/pipeline`, `/pipeline/` с редиректом на дашборд
  - [x] 2.4 Добавление тестов контроллера `CrawlerOpsControllerTest`
- [x] 3. Реализовать веб-дашборд Ingestion Pipeline Dashboard & Thresholds Monitor (`crawler-ops-dashboard.html`)
  - [x] 3.1 Разработка темного интерфейса в стиле SmartBet.guru с executive KPI карточками (Total Bookmakers, Golden Rule #8 Compliance, Ingestion Flow, Alerts)
  - [x] 3.2 Интерактивная визуализация схемы Ingestion Pipeline (Crawlers -> Kafka -> Ingestion -> PostgreSQL -> Arbitrage Scanner) с live-метриками на узлах
  - [x] 3.3 Таблица мониторинга порогов (Thresholds Monitor Table) с поиском, фильтрами по статусам (Compliant, Degraded, Defect, Delayed), прогресс-барами до 500 матчей и бейджами прокси-маршрутов
  - [x] 3.4 Модальное окно детализации БК (распределение по видам спорта, топовые лиги, задержка) и оперативные кнопки (Refresh, Auto-refresh 5s/10s/30s)
  - [x] 3.5 Связка с Entity Resolution Hub (`entity-resolution-hub.html`) через общую панель навигации
- [ ] 4. Сборка, верификация, валидация OpenSpec и деплой
  - [x] 4.1 Компиляция и тестирование модуля `igaming-analytics-service`
  - [x] 4.2 Проверка валидатором `validate_openspec_specs.py`
  - [x] 4.3 Сборка Jib OCI-образа `100.78.183.101:30500/igaming-analytics-service:latest` в кластерный реестр
  - [ ] 4.4 Деплой в Kubernetes namespace `igaming-dev` и проверка Definition of Done
    - [x] 4.4.1 Применение K8s манифеста и проверка статуса пода `Running 1/1`
    - [x] 4.4.2 Проверка Actuator health `/actuator/health/readiness` и `/actuator/health/liveness`
    - [ ] 4.4.3 Выдержка 5-минутного окна тестирования (Soak & Log Inspection) через `schedule` без единой ошибки

