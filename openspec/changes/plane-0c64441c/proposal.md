# Proposal: [STALE] Букмекер 888Starz перестал присылать данные (лаг 17.3 мин)

## Context
Plane Task ID: `0c64441c-4b43-4bd8-a2e7-93d82fe7ce54`

## Problem Statement
В системе мониторинга зафиксирован инцидент с прекращением поступления данных от букмекера 888Starz (`888starz`) с расчетным лагом 17.3 мин (порог: 15.0 мин).
Необходимо провести комплексный аудит источника `igaming-source-888starz`, верифицировать работоспособность краулера, лоадера и базы данных в кластере Kubernetes (`igaming-source`), проверить отсутствие блокировок Cloudflare/WAF и проксирование через роутер `100.83.113.50:3128`, проверить соблюдение критерия наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок.

## Proposed Changes
1. **Верификация источника данных 888Starz**:
   - Проверка статуса подов `igaming-source-888starz-crawler`, `igaming-source-888starz-loader` и базы данных `igaming-source-888starz-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` - UP).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names вместо прямых IP, `ddl-auto=update`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1523 матча, из них 442 live).
   - Проверка актуальности данных в БД источника (лаг < 5 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: 27 068 котировок, лаг < 1 сек).
2. **Мониторинг стабильности сбора линии и soak-тест**:
   - 5-минутный soak-контроль работы подов без сбоев и фатальных ошибок (OOMKilled, NPE, CrashLoop).
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
