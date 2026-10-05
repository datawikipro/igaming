# Proposal: [SUPER-ARB] Аномальная вилка 22.4% с участием Atg

## Context
Plane Task ID: `282597c1-fc1c-4ece-b72c-20863359f0ec`

## Description
Анализ и устранение причин возникновения алерта об аномальной вилке 22.4% с участием букмекера Atg (`igaming-source-atg`).

### Результаты анализа и локализация проблемы:
1. **Причина аномальной вилки**:
   - В фиде Kambi шведский букмекер ATG предоставляет рынки европейских 3-исходных гандикапов ("3-Way Handicap", "Handikapp (3-vägs)") и 3-исходных тоталов ("3-Way Total Goals", содержащих исходы `OT_EXACTLY` / `Precis` / `Exact`).
   - При отсутствии явного подавления такие рынки проходили мимо специализированных обработчиков в `processOutcomeFallback` и попадали под регулярные правила `map()`, где исходы `Over 2` и `Under 2` мапились как стандартные 2-исходные `TotalBet`, а форы — как 2-исходный `HandicapBet`.
   - В агрегаторе сопоставление 3-исходной форы/тотала с 2-исходной линией другого букмекера вызывало расчет фиктивной (аномальной) вилки 22.4% из-за несовместимых вероятностей и условий возврата.

2. **Внесенные исправления в `igaming-source-atg`**:
   - В `AtgOddsMapper` добавлены специализированные методы строгой проверки: `isEuropeanOr3WayHandicap(...)` и `is3WayTotal(...)`.
   - Добавлено подавление на входе `processBetOffer`, в fallback-обработчике `processOutcomeFallback`, в `resolveBetType` и в базовом методе `map(...)`.
   - В `AtgMoneylineHandler` расширены ключевые слова исключения (`HANDIKAPP`, `ANTAL`, `ÖVER/UNDER`).
   - В `AtgHandicapHandler` и `AtgTotalHandler` подтверждено подавление предложений с ничейными исходами форы (`OT_DRAW`, `OT_CROSS`, `OT_TIE`, `Tie`, `Oavgjort`) и точными тоталами (`OT_EXACTLY`, `Precis`).

3. **Верификация**:
   - Интеграционный тест `testEuropean3WayHandicapAnd3WayTotalsSuppression` в `AtgLoadIntegrationTest` проверяет, что 3-way гандикапы и тоталы полностью подавляются, а валидный 2-way азиатский гандикап корректно преобразуется в `HandicapBet`.
   - Все 25 тестов модуля `igaming-source-atg` (22 unit-теста в `AtgOddsMapperTest` и 3 нагрузочных/интеграционных теста в `AtgLoadIntegrationTest`) успешно проходят без ошибок.
