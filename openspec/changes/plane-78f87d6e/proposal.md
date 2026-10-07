# Proposal: [SUPER-ARB] Аномальная вилка 10.1% с участием Bettery

## Context
Plane Task ID: `78f87d6e-2c84-4ae3-9fe5-e8ebb56b8301`

## Problem Statement
В процессе мониторинга и валидации арбитражных ситуаций в ядре `igaming-aggregator-surebet` зафиксирована арбитражная ситуация высокой доходности (10.1%) с участием букмекера Bettery.

В системе обнаружения вилок необходимо гарантировать выполнение двух ключевых требований:
1. **Предотвращение фантомных клоновых арбитражей (ALL_LEGS_SAME_SYNDICATE_CLONE)**: Букмекер Bettery входит в синдикат Fonbet (общая линия, коэффициенты и риск-менеджмент). Вилки между клонами одного синдиката (например, `bettery.ru` и `fon-bet-ru`, либо `беттери` и `pari`) являются нереализуемыми и должны отсекаться правилом `CloneSyndicateRule`.
2. **Сохранение валидных высокодоходных вилок (No Yield Cap)**: Легитимные вилки с участием Bettery и независимых букмекеров (например, Bettery vs Winline) с доходностью $\ge 10\%$ не должны блокироваться или отбрасываться, а должны успешно регистрироваться со статусом `ACTIVE` и направляться в систему оповещений `SurebetAlertManager`.

## Proposed Changes
1. **Расширение алиасов синдиката FONBET в `CloneSyndicateRule`**:
   - Добавление вариантов доменных и кириллических алиасов: `bettery-com`, `bettery.com`, `bettery_com`, `беттери`, `беттери-ру`, `беттери.ру` в `BOOKMAKER_FAMILIES`.
   - Обновление префиксной нормализации в методе `normalizeBookmaker(String bookmaker)` для учета кириллицы и составных имен (`norm.startsWith("bettery") || norm.startsWith("беттери")`).
2. **Юнит-тестирование в `SurebetRuleEvaluatorTest`**:
   - Добавление теста `shouldPreserveBetteryHighYieldSurebet` на сохранение вилки 10.1% между Bettery (TOTAL_OVER 2.5 @ 2.00) и Winline (TOTAL_UNDER 2.5 @ 2.506).
   - Добавление тестов `shouldRejectBetteryRuAndFonbetRuCloneSyndicateSurebet` и `shouldRejectRussianBetteryAndPariCloneSyndicateSurebet` на отклонение внутрисиндикатных связок.
3. **Верификация в Kubernetes и Definition of Done**:
   - Проверка работоспособности сервиса `igaming-aggregator-surebet` в namespace `igaming-dev` (`Running 1/1`, Actuator health HTTP 200 `UP`, аптайм без рестартов).
   - Проверка наполнения линии Bettery ($\ge 500$ матчей в базе данных).
