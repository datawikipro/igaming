# Proposal: [SUPER-ARB] Аномальная вилка 21.0% с участием BetRivers

## Context
Plane Task ID: `a894100b-a894-4a89-8a89-a894100ba894`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирована аномальная вилка высокой доходности (21.0%) с участием американского букмекера BetRivers (`betrivers`), функционирующего на платформе Kambi API (`rsiusny` market `US-NY`).
Необходимо провести комплексный аудит источника данных BetRivers:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-betrivers`, StatefulSet `igaming-source-betrivers-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap;
- валидировать канонические спецификации OpenSpec и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных BetRivers**:
   - Проверка статуса пода `igaming-source-betrivers` (1/1 Running, 0 сбоев, аптайм > 26 часов) и базы данных `igaming-source-betrivers-db-0` (1/1 Running, аптайм > 3 дней) в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness`, `/actuator/health/liveness` на порту 8080 — HTTP 200 UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 945 матчей, 778 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 12 158 котировок, 1 931 за последние 5 минут, heartbeat `bet_source` активен, лаг ~ 1 мин).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names, `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Подтверждение архитектурной изоляции: BetRivers функционирует на провайдере Kambi API и независим от синдиката BetB2B/1XBET, что делает межбукмекерские вилки исполнимыми.
   - Подтверждение политики No Yield Cap (сохранение математически валидных вилок 21.0% без искусственного срезания доходности) и No Silent Drop (логирование в телеметрию без замалчивания).
   - Проверка маппинга рынков (1X2, Total, Handicap) и маршрутизации через кластерный HTTP-прокси (`100.83.113.50:3128`).
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
