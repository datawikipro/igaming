# Proposal: [STALE] Букмекер BetLabel перестал присылать данные (лаг 636.3 мин)

## Context
Plane Task ID: `6bbf15ee-0000-0000-0000-000000000000`

## Problem Statement
В системе мониторинга агрегатора и отслеживания лагов источников котировок зарегистрирован инцидент `[STALE] Букмекер BetLabel перестал присылать данные (лаг 636.3 мин)`.
Букмекер BetLabel (`betlabel`) построен на базе BetB2B API (`igaming-source-betb2b`) и развернут в Kubernetes namespace `igaming-source`:
1. `igaming-source-betlabel-crawler` (краулер линии/лайва с профилем `XVFB_HEADED` / `service-api`).
2. `igaming-source-betlabel-loader` (лоадер матчей и отправка котировок в Kafka топик `odds.updates` и HTTP-хартбитов в агрегатор).
3. `igaming-source-betlabel-db` (PostgreSQL StatefulSet `igaming_betlabel`).

Требуется выполнить полный комплекс диагностических и восстановительных работ:
- Проверить текущий статус компонентов в кластере Kubernetes (`igaming-source`), Actuator health-пробы (`/actuator/health`, `readiness`, `liveness` на порту 3082).
- Проверить сетевую связность через кластерный HTTP-прокси `http://100.83.113.50:3128` (ru-proxy sing-box) к `betlabel.com`.
- Проверить наполнение линии в БД `igaming_betlabel` (выполнение порога $\ge 500$ активных матчей) и лаг обновления.
- Убедиться в поступлении котировок в `igaming-aggregator` (таблицы `bet_source` и `odds_actual`, лаг $< 5$ секунд).
- Провести 5-минутный soak-мониторинг стабильности и валидацию OpenSpec.

## Proposed Changes
1. **Диагностика и верификация K8s подов**:
   - Подтвердить статус `Running` для crawler, loader и db-0.
   - Проверить Actuator health-эндпоинты на порту 3082 (`HTTP 200 UP`).
   - Подтвердить соблюдение Golden Rules (неблокирующий старт HikariCP, строго K8s DNS Service Names, запрет хардкода IP).
2. **Верификация сетевого проксирования и сбора данных**:
   - Проверить обращение к `betlabel.com/service-api/LineFeed/Get1x2_Zip` и `LiveFeed`.
   - Проверить логи `VpnManagerService` на отсутствие блокировок.
3. **Контроль наполнения линии и ликвидации лага**:
   - Валидировать количество матчей в `match_cache` ($\ge 500$).
   - Проверить задержку `NOW() - max(updated_at)`.
   - Проверить таблицу `odds_actual` и статус `is_active` в `bet_source` агрегатора.
4. **Валидация спецификаций**:
   - Запустить `scripts/validate_openspec_specs.py` и подтвердить успешную валидацию.
