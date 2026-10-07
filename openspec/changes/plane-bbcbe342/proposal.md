# Proposal: #776: [STALE] Букмекер 1xBet перестал присылать данные (лаг 16.0 мин)

## Context
Plane Task ID: `bbcbe342-b7d6-43a4-9e4d-7528d7b85ae3`

## Problem Statement
В системе мониторинга зафиксирован инцидент с отставанием линии флагманского букмекера 1xBet (`1xbet`) со статусом STALE (лаг 16.0 мин).
Букмекер 1xBet функционирует на семействе сервисов BetB2B/1xBet (`igaming-source-betb2b`) и обслуживается микросервисами `igaming-source-1xbet-crawler`, `igaming-source-1xbet-loader` и выделенной базой данных PostgreSQL `igaming-source-1xbet-db-0` в Kubernetes namespace `igaming-source`.

Для надежной работы и исключения простоя линии 1xBet требовалось:
1. Обеспечить явный маппинг базового URL для `1xbet` в `Betb2bService.java` (`case "1xbet" -> "https://1xbet.com"`).
2. Зафиксировать доменные имена (включая зеркало `1x-bet.com`) в `app.browser.pre-visit-keywords` в `application.properties` модуля `igaming-source-betb2b`.
3. Добавить спецификацию Actuator health-проб (`startupProbe`, `livenessProbe`, `readinessProbe`) в K8s-манифест `igaming-k8s/1xbet.yaml` для соответствия Golden Rules из AGENTS.md.
4. Провести аудит и подтвердить покрытие тестами маппера семейства 1xBet (`XbetFamilyMapperTest`).
5. Проверить наполнение линии ($\ge 500$ матчей в `match_cache`) и устранение лага котировок в ядре агрегатора `igaming-aggregator`.

## Proposed Changes
1. **Обновление логики и манифестов 1xBet**:
   - Явное определение `case "1xbet" -> "https://1xbet.com"` в `Betb2bService.java`.
   - Добавление `1x-bet.com` в `app.browser.pre-visit-keywords` в `igaming-source-betb2b`.
   - Внедрение Actuator health probes (`startupProbe`, `livenessProbe`, `readinessProbe` на порт 3049) в `igaming-k8s/1xbet.yaml`.
2. **Верификация работоспособности сервиса и ликвидации лага**:
   - Проверка подов `igaming-source-1xbet-crawler` (2/2 Running), `igaming-source-1xbet-loader` (2/2 Running) и базы `igaming-source-1xbet-db-0` (1/1 Running).
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` HTTP 200 `UP`).
   - Контроль объема линии: `match_cache` $\ge 500$ матчей (фактически 1,417 матчей).
   - Контроль агрегатора: таблица `odds_actual` содержит актуальные котировки 1xBet (>69,000 записей), расчетный лаг сокращен с 16.0 мин до ~3.1 сек (< 60 сек).
3. **OpenSpec валидация**:
   - Оформление спецификаций `proposal.md`, `design.md`, `tasks.md`.
   - Валидация через `scripts/validate_openspec_specs.py`.
