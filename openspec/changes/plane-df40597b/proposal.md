# Proposal: [SUPER-ARB] Аномальная вилка 10.5% с участием Stoiximan

## Context
Plane Task ID: `df40597b-bb99-4f9b-a6d0-8924a5bb9992`

## Problem Statement
В системе мониторинга арбитражных ситуаций зафиксирована аномальная вилка высокой доходности (10.5%) с участием греческого лицензированного букмекера Stoiximan (`igaming-source-stoiximan`), функционирующего на базе Kambi API.
Необходимо провести комплексный аудит источника данных Stoiximan, подтвердить стабильность его функционирования в Kubernetes (`igaming-source`), проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать корректность доставки котировок в ядро `igaming-aggregator` (`odds_actual`), проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных Stoiximan**:
   - Проверка статуса подов `igaming-source-stoiximan` и StatefulSet `igaming-source-stoiximan-db-0` в Kubernetes namespace `igaming-source` (`Running 1/1`, `Running 1/1`, 0 сбоев, аптайм 12+ часов).
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` на порту 8080 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1406 матчей, 917 обновлены за последние 10 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 7853 котировок, 1889 за последние 5 минут, heartbeat `bet_source` активен, лаг < 2 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка связок исходов с платформой Kambi и валидация маппинга рынков.
   - Подтверждение политики No Yield Cap (отсутствие искусственного срезания доходности).
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
