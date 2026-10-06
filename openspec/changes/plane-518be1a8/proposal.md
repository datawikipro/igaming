# Proposal: [HIGH LAG] Критическое отставание линии FanSport (30.6 мин)

## Context
Plane Task ID: `518be1a8-c301-4475-b6d4-d50d0364f9bf`

## Problem Statement
В системе мониторинга агрегатора был зафиксирован алерт критического отставания линии букмекера FanSport (30.6 мин).
Необходимо провести полную диагностику источника данных `igaming-source-fansport`, верифицировать текущее состояние подов краулера, лоадера и базы данных `igaming_fansport` в Kubernetes, проверить наполнение линии матчей (критерий Threshold >= 500 матчей), актуальность обновлений котировок, сетевое взаимодействие с BetB2B API и передачу данных в `igaming-aggregator`.

## Proposed Changes
1. **Диагностика источника данных и верификация актуальности линии FanSport**:
   - Аудит текущего состояния базы данных `igaming_fansport` (таблица `match_cache`).
   - Проверка выполнения Golden Rule #8 (Threshold $\ge 500$ активных матчей).
   - Верификация временных меток последних обновлений котировок (`updated_at`, `NOW() - updated_at`).
   - Верификация присутствия и свежести котировок в `igaming-aggregator` (`odds_actual`).
2. **Мониторинг логов краулера-лоадера и проверка метрик в K8s**:
   - Верификация статуса подов `igaming-source-fansport-crawler`, `igaming-source-fansport-loader` и `igaming-source-fansport-db-0` в namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness` и `/actuator/health/liveness`).
   - Анализ логов discovery и scrape-циклов на отсутствие критических сбоев и сетевых тайм-аутов.
   - Прохождение 5-минутного окна бессбойной работы (Soak time).
   - Прогон unit- и интеграционных тестов маппера и лоадера BetB2B/FanSport.
3. **Валидация OpenSpec и фиксация отчета задачи**:
   - Оформление спецификаций согласно стандарту Spec-Driven Development.
   - Валидация через `scripts/validate_openspec_specs.py`.
