# Proposal: [SUPER-ARB] Аномальная вилка 29.2% с участием BetM

## Context
Plane Task ID: `c318ae03-5710-4bbb-8e8d-d44a660fbcf2`

## Problem Statement
В процессе мониторинга арбитражных ситуаций в ядре агрегатора (`igaming-aggregator-surebet`) была зафиксирована аномальная вилка доходностью 29.2% с участием букмекера BetM (платформа Betcity).
Необходимо провести аудит источника данных `igaming-source-betm`, верифицировать работоспособность краулера и загрузчика в Kubernetes (`igaming-source`), проверить критерий наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать причины возникновения аномального арбитража (срабатывание телеметрии `EXTREME_SUREBET`, валидация коэффициентов и маппинга исходов) и обеспечить соответствие архитектурным политикам No Yield Cap и No Silent Drop.

## Proposed Changes
1. **Верификация источника данных BetM**:
   - Проверка статуса подов `igaming-source-betm-crawler`, `igaming-source-betm-loader` и базы данных `igaming-source-betm-db` в кластере K8s.
   - Проверка Actuator health-проб `/actuator/health/readiness` и `/actuator/health/liveness` (UP).
   - Подтверждение наполнения линии ($\ge 500$ активных матчей в `match_cache`, текущее значение 4233 матча).
2. **Аудит аномального арбитража и маппинга исходов**:
   - Проверка регистрации телеметрии `EXTREME_SUREBET` в таблице `odds_anomaly` при превышении порогов доходности (>30.0% в лайве, >50.0% в прематче).
   - Верификация работы правил валидации связок (`CloneSyndicateRule`, `DoubleChanceDominanceRule`, `InvertedOutcomeRule`).
   - Исключение ложных срабатываний и подтверждение соблюдения политики No Yield Cap.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py`.
