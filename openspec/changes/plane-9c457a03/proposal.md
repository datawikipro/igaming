# Proposal: [SUPER-ARB] Аномальная вилка 19.0% с участием BetM

## Context
Plane Task ID: `9c457a03-9235-4310-84c5-162067602f3e`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирован инцидент с аномальной вилкой высокой доходности (19.0%) с участием букмекера BetM (`igaming-source-betm`), функционирующего на базе платформы Betcity (`BetcityEventDiscoverer`, `BetcityOddsProcessor`).
Необходимо провести комплексный аудит источника данных BetM:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-betm-crawler`, `igaming-source-betm-loader`, `igaming-source-betm-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap;
- сформировать спецификации OpenSpec (`proposal.md`, `design.md`, `tasks.md`), валидировать их через `scripts/validate_openspec_specs.py` и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных BetM**:
   - Проверка статуса подов `igaming-source-betm-crawler`, `igaming-source-betm-loader` и StatefulSet `igaming-source-betm-db-0` в Kubernetes namespace `igaming-source` (`Running 2/2`, `Running 1/1`, 0 сбоев).
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` на порту 3048 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 4364 матча, 4209 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 97688 котировок, 14997 за последние 5 минут, heartbeat `bet_source` активен, лаг < 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка и верификация работы `CloneSyndicateRule` для предотвращения ложных внутрисиндикатных арбитражей BETCITY (`betcity`, `betcity-ru`, `betcity-com`, `betcitynl`, `betm`).
   - Подтверждение политики No Yield Cap (сохранение математически валидных вилок высокой доходности без искусственного срезания доходности).
   - Покрытие юнит-тестами в `SurebetRuleEvaluatorTest` и успешное прохождение test suite `aggregator-surebet` (28 тестов пройдены успешно).
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
