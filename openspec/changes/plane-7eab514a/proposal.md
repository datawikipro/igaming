# Proposal: [HIGH LAG] Критическое отставание линии Svenskaspel (284.7 мин)

## Context
Plane Task ID: `7eab514a-ed3e-42d6-af2e-dfdee4670266`

## Problem Statement
В системе мониторинга зафиксирован инцидент с критическим отставанием линии букмекера Svenskaspel (`svenskaspel`) с расчетным лагом 284.7 мин.
Необходимо провести комплексный аудит источника `igaming-source-svenskaspel`, верифицировать работоспособность краулера и БД в кластере Kubernetes (`igaming-source`), проверить соблюдение критерия наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок.

## Proposed Changes
1. **Верификация источника данных Svenskaspel**:
   - Проверка статуса подов `igaming-source-svenskaspel` и базы данных `igaming-source-svenskaspel-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` - UP).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names вместо прямых IP, `ddl-auto=update`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1126 матчей, из них 403 live).
   - Проверка актуальности данных в БД источника (лаг < 10 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: 1562 котировки, лаг < 15 сек).
2. **Мониторинг стабильности сбора линии и soak-тест**:
   - 5-минутный soak-контроль работы пода без сбоев и ошибок (OOMKilled, NPE, CrashLoop).
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
