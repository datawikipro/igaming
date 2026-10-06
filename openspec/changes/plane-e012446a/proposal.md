# Proposal: [SUPER-ARB] Аномальная вилка 10.7% с участием 888Starz

## Context
Plane Task ID: `e012446a-72ef-42d4-b91d-5b32e012446a`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирована аномальная вилка высокой доходности (10.7%) с участием оффшорного букмекера 888Starz (`igaming-source-888starz`), функционирующего на платформе BetB2B (семейство 1xBet).
Необходимо провести комплексный аудит источника данных 888Starz:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-888starz-crawler`, `igaming-source-888starz-loader`, `igaming-source-888starz-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap;
- валидировать канонические спецификации OpenSpec и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных 888Starz**:
   - Проверка статуса подов `igaming-source-888starz-crawler`, `igaming-source-888starz-loader` и StatefulSet `igaming-source-888starz-db-0` в Kubernetes namespace `igaming-source` (`Running 2/2`, `Running 1/1`, 0 сбоев).
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` на порту 3055 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1380 матчей, 1083 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 39896 котировок, 3712 за последние 5 минут, heartbeat `bet_source` активен, лаг < 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка и усиление работы `CloneSyndicateRule` для предотвращения ложных внутрисиндикатных арбитражей BetB2B (`1xbet`, `melbet`, `megapari`, `linebet`, `betandyou`, `fansport`, `888starz`, `spinbetter`).
   - Подтверждение политики No Yield Cap (сохранение математически валидных вилок 10.7% без искусственного срезания доходности).
   - Покрытие юнит-тестами в `SurebetRuleEvaluatorTest` и успешное прохождение test suite `aggregator-surebet`.
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
