# Proposal: [HIGH LAG] Критическое отставание линии Wplay (31.8 мин)

## Context
Plane Task ID: `ee868ad3-0ec8-46bb-90eb-67e648c19bdd`

<<<<<<< HEAD
## Description

=======
## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии колумбийского букмекера Wplay (`wplay`) с расчетным лагом 31.8 мин (порог: 30.0 мин).
Необходимо провести комплексный аудит источника `igaming-source-wplay`, проверить статус подов и базы данных в Kubernetes namespace `igaming-source`, проверить доступность API `apuestas.wplay.co`, верифицировать соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить ликвидацию отставания линии в соответствии с Definition of Done.

## Proposed Changes
1. **Верификация источника данных Wplay и ликвидация отставания**:
   - Проверка статуса подов `igaming-source-wplay` и базы данных `igaming-source-wplay-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 `UP`).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names (`igaming-source-wplay-db.igaming-source.svc.cluster.local`) без прямых IP, `ddl-auto=update`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: $\ge 510$ матчей).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 3,376 котировок, лаг < 20 сек).
2. **Мониторинг стабильности сбора линии и 5-минутный soak-тест**:
   - 5-минутный soak-контроль работы пода без сбоев и фатальных ошибок (0 NPE, 0 OOMKilled, 0 IllegalStateException, 0 CrashLoopBackOff).
   - Верификация непрерывного потока котировок и поддержания актуальности статуса `is_active=true` в агрегаторе.
3. **OpenSpec валидация и фиксация спецификаций**:
   - Формирование полного комплекта спецификаций OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
>>>>>>> feature/plane-e8c00142
