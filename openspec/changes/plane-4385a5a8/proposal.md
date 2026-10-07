# Proposal: [SUPER-ARB] Аномальная вилка 25.3% с участием Betcity.com

## Context
Plane Task ID: `4385a5a8-70dc-4158-bd16-643c8d7fb4c0`

## Problem Statement
В процессе мониторинга арбитражных ситуаций в ядре агрегатора `igaming-aggregator` зафиксирована аномальная вилка доходностью 25.3% с участием букмекера Betcity.com (`betcity-com`).
Необходимо провести аудит источника данных `betcity-com`, проверить работоспособность микросервисов в Kubernetes namespace `igaming-source` (статус подов, Actuator health probes, выполнение критерия наполнения линии $\ge 500$ матчей, минимальный лаг обновления, доставка котировок в `odds_actual`), протестировать логику парсинга и маппинга исходов (18 тестов), проанализировать причину возникновения вилки (срабатывание телеметрии `EXTREME_SUREBET` в `odds_anomaly`, валидация правил `CloneSyndicateRule`, политика No Yield Cap) и валидировать спецификацию OpenSpec.

## Proposed Changes
1. **Верификация источника данных и работоспособности сервиса Betcity.com**:
   - Проверка статуса подов `igaming-source-betcity-com-crawler` (2/2 Running), `igaming-source-betcity-com-loader` (2/2 Running), `igaming-source-betcity-com-db-0` (1/1 Running) в namespace `igaming-source`.
   - Проверка Actuator health-проб `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` (HTTP 200 UP).
   - Контроль объема линии: `match_cache` содержит 4,505 активных матчей (порог $\ge 500$ выполнен с 9-кратным запасом), лаг обновления 0.36 с.
   - Контроль агрегатора: таблица `odds_actual` в `igaming_aggregator` содержит 77,077 актуальных котировок `betcity-com` со свежим лагом 1.96 с.
   - Запуск модульных тестов: успешное прохождение всех 18 тестов в `igaming-source-betcity` (`BetcityParsingTest`, `BetcityMappersTest`).
   - Аудит манифестов K8s: `igaming-k8s/betcity.com.yaml` на предмет использования исключительно K8s DNS Service Names, прокси-топологии (`100.83.113.50:3128`), неблокирующего старта HikariCP и режима `synchronous_commit = off`.
2. **Аудит аномального арбитража телеметрии экстремальных вилок и маппинга исходов**:
   - Анализ телеметрии `EXTREME_SUREBET` в таблице `odds_anomaly` при превышении порога доходности.
   - Проверка работы `CloneSyndicateRule` для предотвращения ложных арбитражей внутри одного семейства/синдиката (`betcity` vs `betcity-com`).
   - Соблюдение принципа No Yield Cap для валидных межбукмекерских арбитражей с высокой доходностью.
3. **Валидация OpenSpec и фиксация спецификаций**:
   - Формирование полного комплекта проектных документов (`proposal.md`, `design.md`, `tasks.md`).
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py plane-4385a5a8`.
