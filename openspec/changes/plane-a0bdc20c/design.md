# Architecture Design: #18: [digitain] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-digitain` принимает котировки через WebSocket Feed Digitain (Melbet) в формате MessagePack.
Ранее маппинг в `DigitainOddsMapper` содержал процедурные ветвления `if-else` только для 5 базовых исходов с фиксированным списком stake type ID.
Цель рефакторинга — внедрить модульную ООП-архитектуру на основе `AbstractBetTypeMapper` и паттерна Chain of Responsibility / Strategy (`DigitainMarketHandler`).

## Component Structure

```
pro.datawiki.igaming.source.digitain.service
├── DigitainOddsMapper (extends AbstractBetTypeMapper, delegates to DigitainMarketHandler list)
└── handler
    ├── DigitainMarketHandler (интерфейс обработчика маркета)
    ├── AbstractDigitainMarketHandler (абстрактный базовый класс с хелперами и валидацией котировок)
    ├── DigitainMatchResultHandler (1X2, Moneyline, исходы таймов)
    ├── DigitainDoubleChanceHandler (Двойной шанс матча и таймов)
    ├── DigitainTotalHandler (Тоталы матча, индивидуальные тоталы, тоталы таймов)
    ├── DigitainHandicapHandler (Форы матча и таймов)
    ├── DigitainBttsHandler (Обе забьют матча и таймов)
    ├── DigitainStatsCornersHandler (Угловые: исходы, тоталы, форы с StatType.CORNERS)
    ├── DigitainStatsCardsHandler (ЖК: исходы, тоталы, форы с StatType.YELLOW_CARDS)
    └── DigitainEsportsHandler (Киберспорт: победители карт MAP_1..3, тотал карт/раундов MAPS/ROUNDS)
```

## Stake Types & Feed Protocol
В `DigitainMatchService` и `DigitainFeedClient` расширяется список запрашиваемых типов ставок (`STAKE_TYPES`):
- 1, 702: Match Winner / 1X2
- 2: Handicap
- 3: Total
- 4, 7: Half 1 / Half 2 1X2
- 5, 8: Half 1 / Half 2 Handicap
- 6, 9: Half 1 / Half 2 Total
- 10, 46: Both Teams To Score
- 166, 167, 168: Corners (Total, Handicap, 1X2)
- 188, 189: Yellow Cards (Total, Handicap, 1X2)
- 703, 704, 705: Map 1/2/3 Winner
- 740, 741: Map Handicap
- 742: Map Total
- 992, 993: Double Chance (Match, 1st Half)

## Testing Strategy
- Разработка unit-тестов в `pro.datawiki.igaming.source.digitain.service.DigitainOddsMapperTest`.
- Проверка покрытия:
  1. Футбол: 1X2, тоталы, форы, обе забьют, двойной шанс;
  2. Статистика: угловые (1X2, тотал, фора), желтые карточки;
  3. Киберспорт: CS2/Dota2 победа по картам, фора карт, тотал карт, тотал раундов.
