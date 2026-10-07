# Proposal: [STALE] Букмекер FanSport перестал присылать данные (лаг 17.3 мин)

## Context
Plane Task ID: `ab7e9658-a47a-417d-9de4-baa874b4f9d3`

## Problem Statement
В системе мониторинга зафиксирован инцидент с прекращением поступления данных от букмекера FanSport (`fansport`) с расчетным лагом 17.3 мин (порог: 15.0 мин).
Необходимо провести аудит источника `igaming-source-fansport`, верифицировать конфигурацию и работоспособность краулера, лоадера и базы данных в кластере Kubernetes (`igaming-source`), проверить отсутствие сетевых блокировок и корректность маршрутизации к BetB2B Family API через кластерный прокси `100.83.113.50:3128`, проверить соблюдение критерия наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок.

## Proposed Changes
1. **Верификация источника данных FanSport**:
   - Проверка статуса подов `igaming-source-fansport-crawler`, `igaming-source-fansport-loader` и базы данных `igaming-source-fansport-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` - HTTP 200 UP).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование исключительно K8s DNS Service Names, `ddl-auto=update`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: > 2000 матчей, из них > 930 live).
   - Проверка актуальности данных в БД источника (лаг < 1 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: > 86 000 котировок, лаг < 1 сек).
2. **Мониторинг стабильности сбора линии и soak-тест**:
   - 5-минутный soak-контроль работы подов без сбоев и фатальных ошибок (OOMKilled, NPE, CrashLoop), факт: аптайм > 20 мин, 0 рестартов.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
