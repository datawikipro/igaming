# Implementation Tasks: [SUPER-ARB] Аномальная вилка 10.1% с участием Bettery

- [x] 1. Интеграция клонового пула Bettery в синдикат FONBET в CloneSyndicateRule
  - [x] 1.1 Добавление алиасов `bettery-com`, `bettery.com`, `bettery_com`, `беттери`, `беттери-ру`, `беттери.ру` в `CloneSyndicateRule.BOOKMAKER_FAMILIES`
  - [x] 1.2 Обновление логики нормализации `normalizeBookmaker` для префиксов `bettery` и `беттери`
- [x] 2. Валидация модульными тестами и сохранение вилки без ограничения доходности
  - [x] 2.1 Добавление теста `shouldPreserveBetteryHighYieldSurebet` на сохранение аномальной арбитражной ситуации 10.1% с участием Bettery
  - [x] 2.2 Добавление тестов `shouldRejectBetteryRuAndFonbetRuCloneSyndicateSurebet` и `shouldRejectRussianBetteryAndPariCloneSyndicateSurebet` на отсечение клонов внутри синдиката FONBET
  - [x] 2.3 Успешный прогон тестов `SurebetRuleEvaluatorTest` (15/15 passed)
- [x] 3. Оформление OpenSpec, верификация в Kubernetes и Definition of Done
  - [x] 3.1 Актуализация артефактов предложения (.openspec.yaml, proposal.md, design.md, tasks.md)
  - [x] 3.2 Валидация спецификаций скриптом `scripts/validate_openspec_specs.py plane-78f87d6e`
  - [x] 3.3 Верификация пода `igaming-aggregator-surebet` в K8s (`Running 1/1`, аптайм > 15 мин, 0 рестартов, Actuator health HTTP 200 UP)
  - [x] 3.4 Верификация критерия наполнения линии (Bettery: 5019 активных матчей $\ge 500$, 220 192 котировки в `odds_actual`)
