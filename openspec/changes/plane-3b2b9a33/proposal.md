# Proposal: [STALE] Букмекер Бетсити перестал присылать данные (лаг 1250.6 мин)

## Context
Plane Task ID: `3b2b9a33-8c4e-4a86-88e3-0f9efb841786`

## Description
Восстановление и верификация работоспособности сервиса сбора линии букмекера Бетсити (betcity / ad.betcity.ru) в Kubernetes namespace `igaming-source`.
Комплексный аудит подов `igaming-source-betcity-crawler`, `igaming-source-betcity-loader`, `igaming-source-betcity-db-0`, проверка Actuator health-проб (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`), проверка наполнения линии (порог >= 500 активных матчей), ликвидация отставания линии (лаг < 15 сек) и верификация передачи актуальных котировок (69k+ исходов) в базу данных ядра агрегации `igaming_aggregator` (`odds_actual`).
