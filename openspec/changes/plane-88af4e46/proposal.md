# Proposal: [HIGH LAG] Критическое отставание линии Atg (645.1 мин)

## Context
Plane Task ID: `88af4e46-ad27-40c9-a450-7b28031347d7`

## Problem Statement
В системе мониторинга агрегатора был зафиксирован алерт критического отставания линии букмекера Atg (645.1 мин).
Необходимо провести полную диагностику источника данных `igaming-source-atg`, верифицировать текущее состояние пода и базы данных `igaming_atg` в Kubernetes, проверить наполнение линии матчей (критерий Threshold >= 500 матчей), актуальность обновлений котировок, сетевое взаимодействие с API Kambi/Atg и передачу данных в `igaming-aggregator`.

## Proposed Changes
1. **Диагностика источника данных и верификация актуальности линии Atg**:
   - Аудит текущего состояния базы данных `igaming_atg` (таблица `match_cache`).
   - Проверка выполнения Golden Rule #8 (Threshold $\ge 500$ активных матчей).
   - Верификация временных меток последних обновлений котировок (`updated_at`, `last_seen`).
2. **Мониторинг логов краулера-лоадера и проверка метрик в K8s**:
   - Верификация статуса подов `igaming-source-atg` и `igaming-source-atg-db-0` в namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness` и `/actuator/health/liveness`).
   - Анализ логов discovery и scrape-циклов на отсутствие исключений (NPE, OOMKilled, Network timeouts).
   - Контроль передачи котировок и heartbeat в `igaming-aggregator`.
3. **Валидация OpenSpec и фиксация отчета задачи**:
   - Оформление спецификаций согласно стандарту Spec-Driven Development.
   - Валидация через `scripts/validate_openspec_specs.py`.
