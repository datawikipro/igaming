# Proposal: [SUPER-ARB] Аномальная вилка 32.9% с участием BetAndYou

## Context
Plane Task ID: `a4d7889f-8a13-46b3-a38e-874da7ab4686`

## Problem Statement
В системе детекции арбитражных ситуаций (surebet engine) зафиксирована аномальная вилка высокой доходности (32.9%) с участием букмекера BetAndYou (`igaming-source-betandyou`), функционирующего на платформе BetB2B (семейство 1xBet).
Необходимо провести комплексный аудит источника данных BetAndYou, подтвердить стабильность его функционирования в Kubernetes (`igaming-source`), проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать корректность доставки котировок в ядро `igaming-aggregator` (`odds_actual`), проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных BetAndYou**:
   - Проверка статуса подов `igaming-source-betandyou-crawler`, `igaming-source-betandyou-loader` и StatefulSet `igaming-source-betandyou-db-0` в Kubernetes namespace `igaming-source` (`Running 2/2`, `Running 1/1`, 0 сбоев).
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` на порту 3053 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1446 матчей, 1117 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 28008 котировок, 2568 за последние 5 минут, heartbeat `bet_source` активен, лаг < 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка работы `CloneSyndicateRule` для предотвращения ложных внутрисиндикатных арбитражей BetB2B (`1xbet`, `melbet`, `megapari`, `linebet`, `betandyou`, `fansport`, `888starz`, `spinbetter`).
   - Подтверждение политики No Yield Cap (отсутствие искусственного срезания доходности).
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
