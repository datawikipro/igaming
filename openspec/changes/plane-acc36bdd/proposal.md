# Proposal: [SUPER-ARB] Аномальная вилка 11.4% с участием Linebet

## Context
Plane Task ID: `acc36bdd-3fde-4da5-8610-6ced6e27d67c`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирована аномальная вилка высокой доходности (11.4%) с участием оффшорного букмекера Linebet (`igaming-source-linebet`), функционирующего на платформе BetB2B (семейство 1xBet).
Необходимо провести комплексный аудит источника данных Linebet:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-linebet-crawler`, `igaming-source-linebet-loader`, `igaming-source-linebet-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap;
- валидировать канонические спецификации OpenSpec и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных Linebet**:
   - Проверка статуса подов `igaming-source-linebet-crawler`, `igaming-source-linebet-loader` и StatefulSet `igaming-source-linebet-db-0` в Kubernetes namespace `igaming-source` (`Running 2/2`, `Running 1/1`, 0 сбоев).
   - Проверка Actuator health-проб (`/actuator/health/readiness`, `/actuator/health/liveness` на порту 3052 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1518 матчей, 1092 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 41298 котировок, 2804 за последние 5 минут, heartbeat `bet_source` активен, лаг < 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка и усиление работы `CloneSyndicateRule` для предотвращения ложных внутрисиндикатных арбитражей BetB2B (`1xbet`, `melbet`, `megapari`, `linebet`, `betandyou`, `fansport`, `888starz`, `spinbetter`).
   - Подтверждение политики No Yield Cap (сохранение математически валидных вилок 11.4% без искусственного срезания доходности).
   - Покрытие юнит-тестами в `SurebetRuleEvaluatorTest` и успешное прохождение test suite `aggregator-surebet`.
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
