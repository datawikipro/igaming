# Proposal: [HIGH LAG] Критическое отставание линии Pari (893.6 мин)

## Context
Plane Task ID: `ff06f291-ac40-4699-9fa3-21b32a2a84d3`

## Problem Statement
Системой мониторинга было зафиксировано критическое отставание котировок линии букмекера Pari (`pari`) с оценкой лага 893.6 минут (порог: 30.0 минут).
Необходимо провести аудит источника данных `igaming-source-pari`, проверить состояние контейнеров `igaming-source-pari-crawler` и `igaming-source-pari-loader`, статус соединения с базой `igaming-source-pari-db` и агрегатором `igaming-aggregator`, валидировать непрерывность потока котировок, соблюдение критерия наполнения линии ($\ge 500$ матчей в `match_cache`), доступность Actuator проб `/actuator/health/readiness` и `/actuator/health/liveness`, а также выдержать 5-минутный интервал стабильной работы без ошибок (Soak Window).

## Proposed Changes
1. **Аудит и диагностика сервиса Pari**:
   - Верификация работы подов `igaming-source-pari-crawler`, `igaming-source-pari-loader` и базы данных `igaming-source-pari-db` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator проб `/actuator/health/readiness` и `/actuator/health/liveness` (HTTP 200 UP).
   - Проверка передачи котировок в Kafka-топик `odds.updates` и сохранение в базу данных агрегатора `igaming_aggregator`.
2. **Верификация наполнения линии и устранения отставания**:
   - Подтверждение наполнения линии в локальной базе `igaming_pari`: `match_cache` $\ge 500$ матчей (актуальное значение: 3,640+ матчей).
   - Подтверждение наличия котировок в ядре агрегатора `igaming_aggregator`: `odds_actual` > 219,000 записей по 5,159+ матчам, постоянное обновление `max(updated_at)` с нулевой задержкой.
   - Проверка статуса в `bet_source`: `is_active = true`, `last_seen` обновляется в реальном времени.
3. **Стандартизация OpenSpec и 5-минутное окно отлежки**:
   - Выполнение требований `AGENTS.md` (Definition of Done, неблокирующий HikariCP, использование K8s DNS Service Names).
   - Подтверждение стабильной бессбойной работы сервиса на протяжении 5-минутного окна наблюдения.
   - Валидация спецификаций через `python3 scripts/validate_openspec_specs.py`.
