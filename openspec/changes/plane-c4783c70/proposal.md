# Proposal: [SUPER-ARB] Аномальная вилка 27.6% с участием Бетсити

## Context
Plane Task ID: `c4783c70-08d8-41dc-b7cc-7d6f984451a5`

## Problem Statement
В процессе агрегации котировок была зафиксирована аномальная арбитражная ситуация (доходность 27.6%) с участием букмекера Бетсити (`betcity` / `betcity-com`).
Необходимо провести комплексный аудит источника `igaming-source-betcity`, верифицировать работоспособность краулера и загрузчика в Kubernetes (`igaming-source`), проверить критерий наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать причину возникновения аномальной доходности (срабатывание телеметрии `EXTREME_SUREBET`, валидация монотонности и маппинга исходов, проверка клонов в `CloneSyndicateRule`) и обеспечить корректную обработку в соответствии с политиками No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных Бетсити**:
   - Проверка статуса подов `igaming-source-betcity-crawler`, `igaming-source-betcity-loader` и базы данных `igaming-source-betcity-db` в кластере K8s namespace `igaming-source`.
   - Проверка Actuator health-проб `/actuator/health` (UP).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ матчей, текущее значение: `igaming_betcity` = 4274 матча, `igaming_betcity_com` = 4289 матчей).
   - Валидация отсутствия падений и перезапусков, подтверждение неблокирующего старта HikariCP и использование K8s DNS Service Names.
2. **Аудит аномального арбитража и маппинга исходов**:
   - Проверка регистрации телеметрии `EXTREME_SUREBET` в `odds_anomaly`.
   - Проверка работы правил валидации связок (`CloneSyndicateRule`, `DoubleChanceDominanceRule`, `InvertedOutcomeRule`).
   - Исключение ложных срабатываний и подтверждение политики No Yield Cap.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py`.
