# Proposal: [STALE] Букмекер FanSport перестал присылать данные (лаг 643.7 мин)

## Context
Plane Task ID: `20ea2fb4-6f02-4217-a006-2581c81cf0ec`

## Problem Statement
В системе мониторинга агрегатора зафиксирован алерт `[STALE] Букмекер FanSport перестал присылать данные (лаг 643.7 мин)`.
Необходимо провести комплексную диагностику источника котировок FanSport (`igaming-source-fansport`), верифицировать статус подов краулера, лоадера и PostgreSQL БД в Kubernetes, проверить соблюдение Definition of Done, наполнение линии активных матчей (Golden Rule #8: Threshold $\ge 500$ матчей), актуальность коэффициентов в `match_cache` и в ядре агрегатора `igaming-aggregator` (`odds_actual`), а также выполнить 5-минутный Soak-тест и валидацию OpenSpec.

## Proposed Changes
1. **Диагностика источника данных и верификация актуальности линии FanSport**:
   - Аудит текущего состояния базы данных `igaming_fansport` (таблица `match_cache`).
   - Проверка выполнения Golden Rule #8 (Threshold $\ge 500$ активных матчей, фактически > 1 500 событий).
   - Верификация временных меток последних обновлений котировок (`updated_at`, `NOW() - updated_at`).
   - Верификация присутствия и свежести котировок в `igaming-aggregator` (`odds_actual`, `bet_source`).
2. **Мониторинг логов краулера-лоадера и проверка метрик в K8s**:
   - Верификация статуса подов `igaming-source-fansport-crawler`, `igaming-source-fansport-loader` и `igaming-source-fansport-db-0` в namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness` и `/actuator/health/liveness`, HTTP 200 UP).
   - Прохождение 5-минутного окна бессбойной работы (Soak time > 150 минут без сбоев и рестартов).
   - Прогон unit- и интеграционных тестов маппера и лоадера BetB2B/FanSport (`XbetFamilyMapperTest`, `Betb2bLoadIntegrationTest`).
3. **Валидация OpenSpec и фиксация отчета задачи**:
   - Оформление спецификаций согласно стандарту Spec-Driven Development.
   - Валидация через `scripts/validate_openspec_specs.py`.
