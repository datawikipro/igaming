# Proposal: [HIGH LAG] Критическое отставание линии Megapari (34.6 мин)

## Context
Plane Task ID: `8d19d27c-117e-44c7-832d-d6c9730d5965`

## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии оффшорного букмекера Megapari (`megapari`) с расчетным лагом 34.6 мин (порог: 30.0 мин).
Megapari функционирует на платформе BetB2B API (`igaming-source-betb2b`) и обслуживается сервисами `igaming-source-megapari-crawler`, `igaming-source-megapari-loader` и базой данных `igaming-source-megapari-db-0` в Kubernetes namespace `igaming-source`.
Необходимо провести комплексный аудит источника `igaming-source-megapari`, верифицировать работоспособность краулера, лоадера и базы данных в кластере Kubernetes, проверить Actuator health-пробы, сетевую связность, соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок в соответствии с Definition of Done.

## Proposed Changes
1. **Верификация источника данных Megapari**:
   - Проверка статуса подов `igaming-source-megapari-crawler`, `igaming-source-megapari-loader` и базы данных `igaming-source-megapari-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 `UP`).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names (`igaming-source-megapari-db.igaming-source.svc.cluster.local`) вместо прямых IP, `ddl-auto=update`, `synchronous_commit=off`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: > 1000 матчей).
   - Проверка актуальности данных в БД источника (лаг < 5 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: > 25 000 котировок, лаг < 35 сек, heartbeat `bet_source` активен).
2. **Мониторинг стабильности сбора линии и soak-тест**:
   - 5-минутный soak-контроль работы подов без сбоев и фатальных ошибок (0 NPE, 0 OOMKilled, 0 IllegalStateException, 0 CrashLoopBackOff).
   - Верификация непрерывного потока котировок в Kafka топик `odds.updates` и стабильности heartbeat в агрегатор.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
