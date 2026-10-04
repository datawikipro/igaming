# Technical Design: [SUPER-ARB] Аномальная вилка 21.8% с участием FanSport

## Architecture & Root Cause Investigation
В рамках анализа алерта об аномальной вилке 21.8% с участием букмекера FanSport (`igaming-source-fansport`) проведена проверка:
1. **Модуль маппинга котировок (`pro.datawiki.igaming.source.betb2b`)**:
   - Маппинг исходов осуществляется через `XbetFamilyMapper` и набор модульных стратегий `XbetFactorStrategy`.
   - Проверена корректность обработки исходов 1X2, фор, общих и индивидуальных тоталов, таймов/периодов, угловых и карточек.
   - Подтверждено отсутствие смешивания таймов или статистических рынков с основным временем матча.
2. **Валидация в `igaming-aggregator-surebet`**:
   - `SurebetValidator` применяет правила:
     - `LivePrematchSeparationRule`: предотвращает арбитраж между Live и Prematch рынками при несовпадении временных рамок.
     - `CloneSyndicateRule`: блокирует вилки между клонами синдиката BetB2B (`1xbet`, `melbet`, `megapari`, `linebet`, `betandyou`, `fansport`, `888starz`, `spinbetter`).
     - `DoubleChanceDominanceRule` и `ComplementaryMarketBoundRule`: предотвращают структурные аномалии.
3. **Нагрузка и стабильность сервиса**:
   - Поды `igaming-source-fansport-crawler` и `igaming-source-fansport-loader` работают стабильно.
   - Actuator-пробы возвращают статус UP.
   - Линия наполнена: 1,304 активных матча в БД, 15,358 котировок в агрегаторе.
