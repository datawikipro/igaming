# Proposal: [SUPER-ARB] Аномальная вилка 36.7% с участием BetAndYou

## Context
Plane Task ID: `65dcbeca-dbfa-45ca-aea7-9a12833d8497`

## Problem Statement
В процессе агрегации котировок была зафиксирована аномальная арбитражная ситуация (доходность 36.7%) с участием букмекера BetAndYou (семейство BetB2B/1xBet).
Необходимо провести комплексный аудит источника `igaming-source-betandyou`, верифицировать работоспособность краулера и загрузчика в Kubernetes (`igaming-source`), проверить критерий наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать причину возникновения аномальной доходности (срабатывание телеметрии `EXTREME_SUREBET`, валидация монотонности и маппинга исходов, проверка клонов в `CloneSyndicateRule`) и обеспечить корректную обработку в соответствии с политиками No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных BetAndYou**:
   - Проверка статуса подов `igaming-source-betandyou-crawler`, `igaming-source-betandyou-loader` и базы данных `igaming-source-betandyou-db` в кластере K8s.
   - Проверка Actuator health-проб `/actuator/health` (UP).
   - Подтверждение наполнения линии ($\ge 500$ матчей, текущее значение > 1300 матчей).
2. **Аудит аномального арбитража и маппинга исходов**:
   - Проверка регистрации телеметрии `EXTREME_SUREBET` в `odds_anomaly`.
   - Проверка работы правил валидации связок (`CloneSyndicateRule`, `DoubleChanceDominanceRule`, `InvertedOutcomeRule`).
   - Исключение ложных срабатываний и подтверждение политики No Yield Cap.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py`.
