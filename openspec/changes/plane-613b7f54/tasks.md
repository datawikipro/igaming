# Implementation Tasks: [HIGH LAG] Критическое отставание линии 1xBit (32.7 мин)
- [x] 1. Исследование причины лага и восстановление потока котировок 1xBit
  - [x] 1.1 Добавление health-проб (startupProbe, livenessProbe, readinessProbe) в манифест `igaming-k8s/1xbit.yaml`
  - [x] 1.2 Прохождение юнит- и интеграционных тестов в модуле `igaming-source-betb2b` (11 тестов: `XbetFamilyMapperTest`, `Betb2bLoadIntegrationTest`)
  - [x] 1.3 Верификация статуса подов в K8s (`igaming-source-1xbit-crawler` 2/2, `igaming-source-1xbit-loader` 2/2, `igaming-source-1xbit-db-0` 1/1)
  - [x] 1.4 Верификация Actuator health-проб (/actuator/health/readiness и /actuator/health/liveness HTTP 200 UP)
  - [x] 1.5 Верификация наполнения линии `match_cache` (1,571 активное событие при пороге >= 500, lag 0.59s)
  - [x] 1.6 Верификация ликвидации лага в ядре агрегатора (таблица `odds_actual`: 80,671 котировка, lag 0.86s < 60s, heartbeat < 5s)
- [x] 2. Мониторинг стабильности и 5-минутный soak-контроль работы сервиса 1xBit
  - [x] 2.1 5-минутный soak-контроль непрерывной работы подов без рестартов (uptime > 5h 25m, 0 restarts)
  - [x] 2.2 Верификация непрерывности потока данных match_cache (1,592 события, lag 2.89s) и odds_actual (81,382 котировки, lag 2.71s)
  - [x] 2.3 Аудит журналов логов (stdout/stderr) на отсутствие фатальных исключений, падений и OOM (авторотация прокси штатно отрабатывает)
  - [x] 2.4 Повторный аудит Actuator health-проб (/actuator/health, /readiness, /liveness HTTP 200 UP на crawler и loader)
- [ ] 3. Валидация OpenSpec и фиксация спецификаций

