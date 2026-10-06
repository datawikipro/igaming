# Proposal: [HIGH LAG] Критическое отставание линии BetM (635.2 мин)

## Context
Plane Task ID: `cba604b6-378f-4653-8fc7-6bd81249cddb`

## Problem Statement
В системе мониторинга агрегатора был зафиксирован алерт `[HIGH LAG] Критическое отставание линии BetM (635.2 мин)`.
Необходимо провести комплексную диагностику источника данных `igaming-source-betm`, верифицировать статус подов краулера, лоадера и базы данных `igaming_betm` в Kubernetes, проверить соблюдение Definition of Done, наполнение линии активных матчей (Golden Rule #8: Threshold $\ge 500$ матчей), актуальность коэффициентов в `match_cache` и в ядре агрегатора `igaming-aggregator` (`odds_actual`), а также выполнить 5-минутный Soak-тест и валидацию OpenSpec.

## Proposed Changes
1. **Диагностика источника данных и верификация актуальности линии BetM**:
   - Аудит текущего состояния базы данных `igaming_betm` (таблица `match_cache`).
   - Проверка выполнения Golden Rule #8 (Threshold $\ge 500$ активных матчей, фактически > 4 400 активных событий).
   - Верификация временных меток последних обновлений котировок (`updated_at`, `NOW() - updated_at`).
   - Верификация присутствия и свежести котировок в `igaming-aggregator` (`odds_actual`, `bet_source`).
2. **Мониторинг логов краулера-лоадера и проверка метрик в K8s**:
   - Верификация статуса подов `igaming-source-betm-crawler`, `igaming-source-betm-loader` и `igaming-source-betm-db-0` в namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness` и `/actuator/health/liveness`, HTTP 200 UP).
   - Прохождение 5-минутного окна бессбойной работы (Soak time > 13 часов без сбоев и рестартов).
   - Прогон unit- и интеграционных тестов маппера и парсера Betcity/BetM (`BetcityParsingTest`, `BetcityMappersTest`).
3. **Валидация OpenSpec и фиксация отчета задачи**:
   - Оформление спецификаций согласно стандарту Spec-Driven Development.
   - Валидация через `scripts/validate_openspec_specs.py`.
