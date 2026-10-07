# Proposal: [SUPER-ARB] Аномальная вилка 33.9% с участием Pinnacle

## Context
Plane Task ID: `38b988d1-b518-472d-9af2-38b988d12345`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирована аномальная вилка высокой доходности (33.9%) с участием ключевого оффшорного букмекера Pinnacle (`igaming-source-pinnacle`).
Необходимо провести комплексный аудит источника данных Pinnacle:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-pinnacle-crawler`, `igaming-source-pinnacle-loader`, `igaming-source-pinnacle-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule` (изоляция связки `pinnacle` и `ps3838`), а также соблюдение политик No Silent Drop и No Yield Cap;
- покрыть регрессионными юнит-тестами в `SurebetRuleEvaluatorTest` и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных Pinnacle**:
   - Проверка статуса подов `igaming-source-pinnacle-crawler`, `igaming-source-pinnacle-loader` и StatefulSet `igaming-source-pinnacle-db-0` в Kubernetes namespace `igaming-source` (`Running 2/2`, `Running 1/1`, 0 сбоев).
   - Проверка Actuator health-проб (`/actuator/health/readiness`, `/actuator/health/liveness` на порту 3040 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 2416 матчей, 2084 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 146696 котировок для Pinnacle, 83452 за последние 5 минут, heartbeat `bet_source` активен, лаг < 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка и верификация работы `CloneSyndicateRule` для предотвращения ложных внутрисиндикатных арбитражей семейства Pinnacle (`pinnacle`, `ps3838`, `pinny`).
   - Подтверждение политики No Yield Cap (сохранение математически валидных вилок 33.9% без искусственного срезания доходности со статусом `ACTIVE`).
   - Покрытие юнит-тестами в `SurebetRuleEvaluatorTest` (`PinnacleSuperArbAuditTests`) и успешное прохождение test suite `aggregator-surebet` (43 теста пройдены успешно).
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
