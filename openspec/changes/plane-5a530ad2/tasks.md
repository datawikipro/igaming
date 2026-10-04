# Implementation Tasks
- [x] 1. Implement [STALE] Букмекер 888Starz перестал присылать данные (лаг 97.5 мин)
  - [x] Анализ статуса подов igaming-source-888starz (crawler, loader, db) в K8s
  - [x] Проверка Actuator probes (/actuator/health, /actuator/health/readiness, /actuator/health/liveness HTTP 200 UP)
  - [x] Проверка наполненности линии (1345 активных матчей в match_cache при пороге >= 500)
  - [x] Проверка потока котировок в агрегатор (1496 актуальных записей в odds_actual, лаг < 1 сек)
  - [x] 5-минутный мониторинг логов (отсутствие ошибок Exception, NPE, Crash, OOMKilled)
