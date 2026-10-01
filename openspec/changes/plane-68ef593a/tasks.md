# Implementation Tasks: [UI-CRAWLER-OPS] Ingestion Pipeline Dashboard & Thresholds Monitor

- [x] 1. Спроектировать архитектуру и спецификации OpenSpec (proposal.md, design.md, .openspec.yaml) для [UI-CRAWLER-OPS]
- [x] 2. Разработать модель данных DTO и сервисный слой CrawlerOpsService в igaming-analytics-service с реестром 52 БК, расчетом метрик инжестинга и порогов наполнения (>= 500 матчей)
- [x] 3. Реализовать REST API контроллер CrawlerOpsController и UI-роутер CrawlerOpsUiController (/crawler-ops)
- [x] 4. Создать интерактивный дашборд crawler-ops-dashboard.html с визуализацией пайплайнов 52 БК, прогресс-барами порогов, живыми счетчиками, алертами и триггерами синхронизации
- [ ] 5. Покрыть unit-тестами (CrawlerOpsServiceTest, CrawlerOpsControllerTest), валидировать сборку Maven и деплой-манифесты
