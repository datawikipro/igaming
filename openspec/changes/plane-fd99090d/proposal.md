# Proposal: [SUPER-ARB] Аномальная вилка 11.2% с участием Betcity.com

## Context
Plane Task ID: `fd99090d-1d29-475c-b240-7667ad3fd05a`

## Problem Statement
В системе обнаружения арбитражных ситуаций (модуль `aggregator-surebet`) была зафиксирована аномальная арбитражная ситуация (супер-вилка) с расчетной доходностью 11.2% (и выше, вплоть до 14.89%) с участием букмекера `Betcity.com` (`betcity.com`, `betcity`, `betcity-ru`, `betm`).
Данные букмекеры используют идентичную линию одного синдиката (`BETCITY`), и разница в котировках между ними вызвана не рыночным арбитражем, а сетевым лагом доставки обновлений между разными доменами и зеркалами букмекера. Возникновение подобных ложных вилок засоряет витрину для пользователей и сигнальные каналы Telegram/ботов.

Необходимо устранить возникновение аномальных вилок между клонами синдиката Betcity:
- Идентифицировать все алиасы и варианты написания доменов синдиката Betcity (`betcity`, `betcity-com`, `betcity.com`, `betcity_com`, `betcity-ru`, `betcity.ru`, `betcity_ru`, `betcity-by`, `betcity.by`, `betcity_by`, `betcitynl`, `betcity-nl`, `betcity.nl`, `betm`, `бетсити`).
- Модернизировать `CloneSyndicateRule` в сервисе `aggregator-surebet`, добавив регистрацию всех вариантов написания и префиксную защиту.
- Покрыть логику регрессионными юнит-тестами в `SurebetRuleEvaluatorTest`.
- Задеплоить обновленный сервис `igaming-aggregator-surebet` в кластер Kubernetes namespace `igaming-dev`.
- Подтвердить фильтрацию аномальных вилок по логам пода и выдержать обязательный 5-минутный soak-тест без сбоев в соответствии с Definition of Done (Golden Rule 1).

## Proposed Changes
1. **Реализация правил валидации синдиката Betcity**:
   - Обновление `CloneSyndicateRule.java`: маппинг всех вариаций доменов Betcity в семейство `BETCITY`, санитайзинг разделителей (`.` и `_` -> `-`), префиксный fallback `norm.startsWith("betcity") -> "BETCITY"`.
   - Добавление unit-тестов в `SurebetRuleEvaluatorTest.java` на взаимные пары `betcity + betcity.com`, `betcity-com + betm`, `betcity.ru + betcitynl`.
   - Прохождение полного тестового набора `aggregator-surebet` (28 тестов, 0 ошибок).
2. **Верификация работы в Kubernetes**:
   - Статус пода `igaming-aggregator-surebet` в namespace `igaming-dev`: `Running 1/1`, 0 рестартов.
   - Actuator health-пробы: `/actuator/health/readiness` и `/actuator/health/liveness` отдают HTTP 200 `{"status":"UP"}`.
   - Подтверждение фильтрации аномальных вилок по логам:
     `Rejecting clone syndicate surebet on match 191989 (Ванкувер Джайантс vs Брендон Уит Кингс) in TOTAL:4.5: all legs belong to family BETCITY ... profit=11.00%`.
   - Проведение 5-минутного непрерывного soak-контроля без ошибок в логах.
3. **OpenSpec валидация**:
   - Формирование полного комплекта спецификаций: `.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`.
   - Успешная валидация через `python3 scripts/validate_openspec_specs.py plane-fd99090d`.
