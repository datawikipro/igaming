# Proposal: [SUPER-ARB] Аномальная вилка 10.7% с участием Pinnacle

## Context
Plane Task ID: `5e806163-98d4-4015-a2f2-fd4aec19b16b`

<<<<<<< HEAD
## Problem Statement
В процессе агрегации котировок была зафиксирована аномальная арбитражная ситуация (доходность 10.7%) с участием европейского/оффшорного букмекера Pinnacle (`pinnacle`).
Необходимо провести комплексный аудит источника `igaming-source-pinnacle`, верифицировать работоспособность краулера и загрузчика в Kubernetes (`igaming-source`), проверить критерий наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать причину возникновения аномальной доходности (срабатывание телеметрии `EXTREME_SUREBET`, валидация монотонности и маппинга исходов в `PinnacleOddsMapper`, проверка клонов в `CloneSyndicateRule`) и обеспечить корректную обработку в соответствии с политиками No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных Pinnacle**:
   - Проверка статуса подов `igaming-source-pinnacle-crawler`, `igaming-source-pinnacle-loader` и базы данных `igaming-source-pinnacle-db` в кластере K8s namespace `igaming-source`.
   - Проверка Actuator health-проб `/actuator/health` (UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ матчей, текущее значение: `igaming_pinnacle` = 1981 матч).
   - Валидация отсутствия фатальных падений, подтверждение неблокирующего старта HikariCP и использование K8s DNS Service Names.
2. **Аудит аномального арбитража и маппинга исходов**:
   - Проверка регистрации телеметрии `EXTREME_SUREBET` в `odds_anomaly`.
   - Проверка работы правил валидации связок (`CloneSyndicateRule`, `DoubleChanceDominanceRule`, `InvertedOutcomeRule`).
   - Исключение ложных срабатываний и подтверждение политики No Yield Cap.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py`.
=======
## Description

>>>>>>> feature/plane-134d0015
