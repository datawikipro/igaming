# Proposal: [STALE] Букмекер Unibet перестал присылать данные (лаг 15.9 мин)

## Context
Plane Task ID: `342b8759-0fb0-459a-a120-f08feac0cef9`

## Problem Statement
В системе мониторинга зафиксирован инцидент с отставанием линии букмекера Unibet (`unibet`) с расчетным лагом 15.9 мин (статус STALE > 15m).
Необходимо провести комплексный аудит источника `igaming-source-unibet`, верифицировать работоспособность пода и БД в Kubernetes (`igaming-source`), проверить соблюдение критерия наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок.

## Proposed Changes
1. **Верификация источника данных Unibet и работоспособности сервиса**:
   - Проверка статуса подов `igaming-source-unibet` и базы данных `igaming-source-unibet-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 UP).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names вместо прямых IP (`igaming-source-unibet-db.igaming-source.svc.cluster.local`), `ddl-auto=update`, `synchronous_commit=off`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1190 матчей, из них 484 live, 706 prematch).
   - Проверка актуальности данных в БД источника (лаг < 5 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: 3577 котировок, лаг < 5 сек, heartbeat `is_active=true`).
2. **Мониторинг стабильности сбора линии и 5-минутный soak-тест**:
   - Контроль 5-минутного окна бессбойной работы пода без сбоев и ошибок (OOMKilled, NPE, CrashLoop).
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
