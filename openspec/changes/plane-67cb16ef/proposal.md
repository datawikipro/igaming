# Proposal: [SUPER-ARB] Аномальная вилка 17.6% с участием Бетсити

## Context
Plane Task ID: `67cb16ef-5962-4d98-aacc-126d7c47843c`

## Problem Statement
В процессе агрегации котировок в ядре `igaming-aggregator-surebet` была зафиксирована арбитражная ситуация с высокой доходностью (17.6%) с участием букмекера Бетсити (`betcity` / `betcity-com`).
Необходимо провести комплексный аудит источника `igaming-source-betcity`, верифицировать работоспособность краулера, загрузчика и базы данных в Kubernetes namespace `igaming-source`, проверить критерий наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать причину возникновения высокой доходности (срабатывание телеметрии `EXTREME_SUREBET`, валидация монотонности и маппинга исходов, проверка изоляции клонов в `CloneSyndicateRule`) и обеспечить корректную обработку в соответствии с непреложными архитектурными политиками No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных Бетсити**:
   - Проверка статуса подов `igaming-source-betcity-crawler`, `igaming-source-betcity-loader` и базы данных `igaming-source-betcity-db` в кластере K8s namespace `igaming-source`.
   - Проверка Actuator health-проб `/actuator/health/liveness` и `/actuator/health/readiness` (HTTP 200 `UP`).
   - Подтверждение выполнения критерия наполнения линии ($\ge 500$ матчей, текущее значение: `igaming_betcity` = 4304 матча, `igaming_betcity_com` = 4303 матча, порог превышен более чем в 8.6 раз).
   - Валидация отсутствия падений и перезапусков, подтверждение неблокирующего старта HikariCP (`initialization-fail-timeout=0`) и использование K8s DNS Service Names (`igaming-source-betcity-db.igaming-source.svc.cluster.local:5432`).
2. **Аудит аномального арбитража и маппинга исходов**:
   - Проверка регистрации телеметрии `EXTREME_SUREBET` в `odds_anomaly` при превышении порога доходности.
   - Проверка работы правил валидации связок (`CloneSyndicateRule`, `ComplementaryMarketBoundRule`, `DoubleChanceDominanceRule`, `InvertedOutcomeRule`).
   - Исключение ложных срабатываний и подтверждение политики No Yield Cap (математически корректные вилки 17.6% публикуются без занижения доходности).
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py`.
