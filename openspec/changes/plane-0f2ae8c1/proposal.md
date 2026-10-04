# Proposal: [SUPER-ARB] Аномальная вилка 21.4% с участием Melbet

## Context
Plane Task ID: `0f2ae8c1`

## Problem Statement
В процессе мониторинга арбитражных ситуаций в `igaming-aggregator-surebet` зафиксирована аномальная вилка с доходностью 21.4% с участием букмекера Melbet (семейство BetB2B / 1XBET).

В таблице `odds_anomaly` были зарегистрированы соответствующие записи:
1. `ALL_LEGS_SAME_SYNDICATE_CLONE` (ID 43906, доходность 21.45%): связка между 1xBet и Melbet (матч Коррекаминос vs Круз Азуль Идальго, 1xBet DC_X2 @ 1.464, Melbet WIN1 @ 9.76).
2. `LIVE_PREMATCH_MARKET_MISMATCH` (ID 34767, доходность 21.44%): связка Melbet vs Betcity на тотале 3.5 (Англия U19 vs США U19).

Также при детальном аудите модуля `igaming-source-betb2b` выявлены скрытые дефекты конфигурации алиасов Melbet:
1. В `XbetFamilyMapper.SUPPORTED_BOOKMAKERS` отсутствовали алиасы `melbet-com` и `melbet.ru`, из-за чего для пода `igaming-source-melbet-com` исходы не распознавались маппером и переходили в UNKNOWN.
2. В `Betb2bService.resolveBaseUrl` отсутствовал case `melbet-com`, из-за чего события Melbet COM получали ссылки на `1xbet.com` вместо `melbet.com`.
3. В `CloneSyndicateRule` отсутствовал алиас `melbet.ru`.

## Proposed Changes
1. **Расширение поддерживаемых алиасов в `XbetFamilyMapper`**:
   - Добавление `melbet-com`, `melbet.ru`, `22bet`, `1xbit`, `1x-bet`, `1xstavka`, `betwinner` в `SUPPORTED_BOOKMAKERS`.
2. **Исправление генерации URL в `Betb2bService`**:
   - Добавление маппинга `melbet-com -> https://melbet.com`, `melbet.ru -> https://melbet.ru`, `spinbetter -> https://spinbetter.com`, `22bet -> https://22bet.com`.
3. **Обновление правил синдиката в `CloneSyndicateRule`**:
   - Добавление `melbet.ru` в синдикат `1XBET`.
4. **Покрытие тестами**:
   - Добавление тестов в `XbetFamilyMapperTest` на поддержку `melbet-com` и `melbet.ru`.
   - Добавление теста в `SurebetRuleEvaluatorTest` на отклонение клонов 1xBet + Melbet через `CloneSyndicateRule`.
5. **Верификация в Kubernetes**:
   - Проверка подов `igaming-source-melbet-*` и `igaming-source-melbet-com-*` в namespace `igaming-source`.
   - Проверка выполнения критерия наполнения линии ($\ge 500$ матчей в `match_cache`).
