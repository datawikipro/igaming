# Proposal: [SUPER-ARB] Аномальная вилка 13.8% с участием 1xBit

## Context
Plane Task ID: `ae86b0a2-e1af-44b5-a373-fe96cba76cba`

## Problem Statement
В подсистеме детекции арбитражных ситуаций (surebet engine ядра `igaming-aggregator`) зафиксирована аномальная вилка высокой доходности (13.8%) с участием международного крипто-букмекера 1xBit (`igaming-source-1xbit`), функционирующего на платформе BetB2B (семейство 1xBet).
Необходимо провести комплексный аудит источника данных 1xBit:
- подтвердить стабильность его функционирования в Kubernetes namespace `igaming-source` (`igaming-source-1xbit-crawler`, `igaming-source-1xbit-loader`, `igaming-source-1xbit-db-0`);
- проверить соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`);
- проанализировать доставку и актуальность котировок в ядро `igaming-aggregator` (`odds_actual`, `bet_source`);
- проверить корректность маппинга исходов в `XbetFamilyMapper` (отсутствие инверсий коэффициентов ТБ/ТМ, перепутанных знаков фор, некорректного маппинга исходов);
- проверить срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, работу правил отсечения клонов в `CloneSyndicateRule`, а также соблюдение политик No Silent Drop и No Yield Cap;
- валидировать канонические спецификации OpenSpec и подтвердить Definition of Done.

## Proposed Changes
1. **Верификация источника данных 1xBit**:
   - Проверка статуса подов `igaming-source-1xbit-crawler` (2/2 Running, 0 рестартов, аптайм > 6 часов), `igaming-source-1xbit-loader` (2/2 Running, 0 рестартов, аптайм > 6 часов) и базы данных `igaming-source-1xbit-db-0` (1/1 Running, аптайм > 3 дней) в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness`, `/actuator/health/liveness` на порту 3059 — HTTP 200 UP) и контейнерных статусов `ready=true`.
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1 682 матча, 1 102 обновлены за последние 5 минут).
   - Проверка актуальности данных в БД источника и доставки котировок в `igaming-aggregator` (`odds_actual`: 85 407 котировок, 1 932 за последние 5 минут, heartbeat `bet_source` активен, лаг ~ 11 секунд).
   - Проверка соблюдения стандартов AGENTS.md: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), K8s DNS Service Names (`igaming-source-1xbit-db.igaming-source.svc.cluster.local`), `ddl-auto=update`, `synchronous_commit=off`.
2. **Анализ аномального арбитража и валидация маппинга исходов**:
   - Аудит детекции арбитражей и фиксации аномалий в `odds_anomaly` (`EXTREME_SUREBET`, `Severity.WARNING`).
   - Проверка маппера исходов `XbetFamilyMapper` и стратегий `XbetMainResultStrategy`, `XbetTotalStrategy`, `XbetHandicapStrategy`: подтверждение симметрии и корректности маппинга (`TOTAL_UNDER` и `TOTAL_OVER` строго симметричны по 9 121 котировке, `HANDICAP_1` и `HANDICAP_2` по 7 391 котировке, средние коэффициенты сбалансированы 1.84 vs 2.03 и 1.97 vs 1.92).
   - Подтверждение работы `CloneSyndicateRule` для отсечения неисполнимых внутрисиндикатных связок между клонами BetB2B/1XBET (`1xbit`, `1xbet`, `melbet`, `megapari`, `linebet`, `betandyou`, `fansport`, `888starz`, `spinbetter`, `22bet`).
   - Верификация политики No Yield Cap: математически валидные вилки доходностью 13.8% с независимыми букмекерами сохраняются в статусе ACTIVE без искусственного усечения.
   - Успешный прогон unit-тестов модуля `igaming-source-betb2b` (11 тестов пройдены успешно без сбоев).
3. **OpenSpec валидация и завершение задачи**:
   - Формирование и актуализация артефактов OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
