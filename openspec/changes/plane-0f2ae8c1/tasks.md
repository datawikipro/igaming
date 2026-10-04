# Implementation Tasks
- [x] 1. Implement [SUPER-ARB] Аномальная вилка 21.4% с участием Melbet
  - [x] Расширение поддерживаемых алиасов в `XbetFamilyMapper.SUPPORTED_BOOKMAKERS` (`melbet-com`, `melbet.ru`, `22bet`, `1xbit`, `1x-bet`, `1xstavka`, `betwinner`)
  - [x] Добавление маппинга URL в `Betb2bService.resolveBaseUrl` (`melbet-com -> https://melbet.com`, `melbet.ru -> https://melbet.ru`)
  - [x] Интеграция `melbet.ru` в синдикат `1XBET` в `CloneSyndicateRule` для отсечения межклоновых арбитражей (21.45%)
  - [x] Валидация модульными тестами (`XbetFamilyMapperTest` 8/8 passed, `SurebetRuleEvaluatorTest` 16/16 passed)
  - [x] Верификация сервисов и наполнения линии в Kubernetes (`igaming_melbet`: 1257 матчей, `igaming_melbet_com`: 1151 матч >= 500)
  - [x] Верификация Definition of Done и 5-минутного окна работы сервиса `igaming-aggregator-surebet` (5m45s+, 0 restarts, HTTP Actuator UP)
