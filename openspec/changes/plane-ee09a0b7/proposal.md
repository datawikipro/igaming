# Proposal: [SUPER-ARB] Аномальная вилка 13.5% с участием Unibet

## Context
Plane Task ID: `ee09a0b7-0761-4104-acd7-cd106912d318`

## Problem Statement
В процессе агрегации котировок была зафиксирована аномальная арбитражная ситуация (доходность 13.5%) с участием европейского/оффшорного букмекера Unibet (`unibet`).
Необходимо провести комплексный аудит источника `igaming-source-unibet`, верифицировать работоспособность краулера и загрузчика в Kubernetes (`igaming-source`), проверить критерий наполнения линии ($\ge 500$ матчей в `match_cache`), проанализировать причину возникновения аномальной доходности (срабатывание телеметрии `EXTREME_SUREBET`, валидация монотонности и маппинга исходов, проверка клонов в `CloneSyndicateRule`) и обеспечить корректную обработку в соответствии с политиками No Silent Drop и No Yield Cap.

## Proposed Changes
1. **Верификация источника данных Unibet и работоспособности сервиса**:
   - Проверка статуса подов `igaming-source-unibet` и базы данных `igaming-source-unibet-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 UP).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names (`igaming-source-unibet-db.igaming-source.svc.cluster.local`), `ddl-auto=update`, `synchronous_commit=off`.
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: 1161 матч).
   - Проверка актуальности данных в БД источника (лаг < 2 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: 5101 котировка, лаг < 2 сек).
2. **Аудит аномального арбитража и маппинга исходов**:
   - Проверка регистрации телеметрии `EXTREME_SUREBET` в `odds_anomaly`.
   - Проверка правил валидации связок (`CloneSyndicateRule`, `DoubleChanceDominanceRule`, `InvertedOutcomeRule`).
   - Исключение ложных срабатываний и подтверждение политики No Yield Cap для валидных арбитражей.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py`.
