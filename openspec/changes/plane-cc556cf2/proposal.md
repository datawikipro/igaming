# Proposal: [HIGH LAG] Критическое отставание линии BetRivers (578.5 мин)

## Context
Plane Task ID: `cc556cf2-95c6-4620-9717-8d57770130cc`

## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии букмекера BetRivers (`betrivers`) с расчетным лагом 578.5 мин (порог: 30.0 мин).
Необходимо провести комплексный аудит источника `igaming-source-betrivers`, верифицировать работоспособность краулера/лоадера и базы данных в кластере Kubernetes (`igaming-source`), проверить сетевую доступность API Kambi и маршрутизацию трафика через кластерный HTTP-прокси `100.83.113.50:3128`, проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок в соответствии с Definition of Done.

## Proposed Changes
1. **Верификация источника данных BetRivers**:
   - Проверка статуса пода `igaming-source-betrivers` и базы данных `igaming-source-betrivers-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 `UP`).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names (`igaming-source-betrivers-db.igaming-source.svc.cluster.local`) вместо прямых IP, `ddl-auto=update`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 926 матчей).
   - Проверка актуальности данных в БД источника (лаг < 60 сек, факт ~18 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: 11 846 котировок, лаг < 1 сек).
2. **Мониторинг стабильности сбора линии и soak-тест**:
   - 5-минутный soak-контроль работы подов без сбоев и фатальных ошибок (0 NPE, 0 OOMKilled, 0 IllegalStateException, 0 CrashLoopBackOff).
   - Верификация непрерывного сбора котировок и стабильности heartbeat в агрегатор.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
