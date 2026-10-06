# Proposal: [HIGH LAG] Критическое отставание линии Fonbet (RU) (897.2 мин)

## Context
Plane Task ID: `b608cd09-820f-4f91-afba-9508e6ffc3ab`

## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии российского букмекера Fonbet (RU) (`fon-bet-ru`) с расчетным лагом 897.2 мин.
Fonbet (RU) функционирует на платформе Fonbet Family (`igaming-source-fon-bet-ru`) и обслуживается сервисами `igaming-source-fon-bet-ru-crawler`, `igaming-source-fon-bet-ru-loader` и базой данных `igaming-source-fon-bet-ru-db-0` в Kubernetes namespace `igaming-source`.
Необходимо провести комплексный аудит источника `igaming-source-fon-bet-ru`, верифицировать работоспособность краулера, лоадера и базы данных в кластере Kubernetes, проверить Actuator health-пробы, сетевую связность, соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок в соответствии с Definition of Done.

## Proposed Changes
1. **Верификация источника данных Fonbet (RU)**:
   - Проверка статуса подов `igaming-source-fon-bet-ru-crawler`, `igaming-source-fon-bet-ru-loader` и базы данных `igaming-source-fon-bet-ru-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 `UP`).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names (`igaming-source-fon-bet-ru-db.igaming-source.svc.cluster.local`) вместо прямых IP, `ddl-auto=update`, `synchronous_commit=off`, кеширование факторов в Redis (`APP_PERSISTENCE_USE_REDIS_FACTORS=true`).
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: > 3900 матчей).
   - Проверка актуальности данных в БД источника (лаг < 1 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: > 250 000 котировок, лаг < 1 сек, heartbeat `bet_source` активен).
2. **Мониторинг стабильности сбора линии и soak-тест**:
   - 5-минутный soak-контроль работы подов без сбоев и фатальных ошибок (0 NPE, 0 OOMKilled, 0 IllegalStateException, 0 CrashLoopBackOff).
   - Верификация непрерывного потока котировок в Kafka топик `odds.updates` и стабильности heartbeat в агрегатор.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
