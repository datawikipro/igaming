# Implementation Tasks
- [ ] 1. Implement [aggregator] Синтетический арбитраж: мульти-букмекерские формулы вилок и коридоров для Чет/Нечет, BTTS, карт CS2 и тоталов статистики
  - [x] 1.1 Расширение нормализации рынков и алиасов в SurebetRuleEvaluator (Чет/Нечет, BTTS, 2-way карты CS2, тоталы и форы статистики)
  - [x] 1.2 Реализация синтетических мульти-букмекерских вилок (BTTS + Clean Sheet Team 1/2) в SurebetRuleEvaluator
  - [x] 1.3 Реализация формул коридоров в MiddleFinderService (статистика, CS2 раунды/карты, синтетические коридоры BTTS+ТМ и ТМ+Нечет)
  - [x] 1.4 Модульные тесты для всех формул вилок и коридоров в SurebetRuleEvaluatorTest и MiddleFinderServiceTest
  - [x] 1.5 Сборка Maven Jib и пуш образа в локальный cluster registry (100.78.183.101:30500/igaming-aggregator-surebet:latest)
  - [ ] 1.6 Деплой и верификация в Kubernetes (igaming-dev): Actuator health check и 5-минутный тест стабильности
