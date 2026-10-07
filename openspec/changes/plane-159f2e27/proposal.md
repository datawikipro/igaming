# Proposal: [SUPER-ARB] Аномальная вилка 10.4% с участием Betcity.com

## Context
Plane Task ID: `159f2e27-12fb-447f-b311-6d6b4af02d0f`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирована аномальная вилка высокой доходности (10.4%) с участием оффшорного букмекера Betcity.com (`igaming-source-betcity-com`).
Необходимо провести комплексный аудит источника данных Betcity.com:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-betcity-com-crawler`, `igaming-source-betcity-com-loader`, `igaming-source-betcity-com-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule` (взаимное клонирование Betcity RU / Betcity COM / Bettery), а также соблюдение политик No Silent Drop и No Yield Cap;
- валидировать канонические спецификации OpenSpec и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных Betcity.com**:
   - Проверка статуса подов `igaming-source-betcity-com-crawler`, `igaming-source-betcity-com-loader` и StatefulSet `igaming-source-betcity-com-db-0` в Kubernetes namespace `igaming-source` (`Running 2/2`, `Running 2/2`, `Running 1/1`).
   - Проверка Actuator health-проб (`/actuator/health/readiness`, `/actuator/health/liveness` на порту 3042 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 4,250 матчей, 4,109 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 77,997 котировок, 1,052 за последние 5 минут, heartbeat `bet_source` активен, лаг < 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка и усиление работы `CloneSyndicateRule` для предотвращения ложных внутрисиндикатных арбитражей семейства Betcity (`betcity`, `betcity-com`, `bettery`).
   - Подтверждение политики No Yield Cap (сохранение математически валидных вилок 10.4% без искусственного срезания доходности).
   - Покрытие юнит-тестами в `SurebetRuleEvaluatorTest` и успешное прохождение test suite `aggregator-surebet`.
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
