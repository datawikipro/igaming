# Architecture Design: [sbobet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-sbobet` осуществляет сбор линий и котировок азиатского букмекера SBOBET.
Цель рефакторинга — переход к полнофункциональной ООП-архитектуре обработчиков рынков (`SbobetMarketHandler`) с поддержкой углубленной росписи исходов, статистических маркетов (угловые, ЖК) и киберспортивных дисциплин (CS2, Dota2, LoL).

## Component Structure

```
pro.datawiki.igaming.source.sbobet.service
├── SbobetOddsMapper (extends AbstractBetTypeMapper, iterates marketHandlers with SportType)
└── handler
    ├── SbobetMarketHandler (интерфейс с поддержкой supports/handle для marketKey и SportType)
    ├── AbstractSbobetMarketHandler (базовый абстрактный класс с хелперами addMatchResult, addTotal, addHandicap, addBinary)
    ├── SbobetMoneylineHandler (1X2, Moneyline, исходы таймов)
    ├── SbobetTotalHandler (Тоталы Over/Under матча и таймов)
    ├── SbobetHandicapHandler (Форы матча и таймов)
    ├── SbobetDoubleChanceHandler (Двойной шанс: 1X, 12, X2 матча и 1-го тайма)
    ├── SbobetBttsHandler (Обе забьют: Yes/No матча и 1-го тайма)
    ├── SbobetDrawNoBetHandler (Ничья нет ставки / Фора 0.0 матча и 1-го тайма)
    ├── AbstractSbobetStatsHandler (базовый класс для статистических маркетов)
    ├── SbobetStatsCornersHandler (Угловые: 1X2, тоталы, форы с StatType.CORNERS)
    ├── SbobetStatsCardsHandler (ЖК: 1X2, тоталы, форы с StatType.YELLOW_CARDS)
    └── SbobetEsportsHandler (Киберспорт: победители карт MAP_1..5, тотал/фора карт StatType.MAPS, раунды StatType.ROUNDS)
```

## Market Handlers & Protocols
- **Основные рынки**: 1X2, Moneyline (2-way / 3-way), тоталы и форы матча и 1-го тайма.
- **Роспись исходов**:
  - `double_chance`, `double_chance_half1`: 1X, 12, X2 исходы;
  - `btts`, `both_teams_to_score`: BTTS Yes/No;
  - `draw_no_bet`, `dnb`: исходы без ничьей (Handicap 0.0).
- **Статистические маркеты**:
  - `corners*`: угловые с `StatType.CORNERS` (исходы, тоталы Over/Under, форы);
  - `cards*`, `yellow_cards*`: желтые карточки с `StatType.YELLOW_CARDS`.
- **Киберспорт**:
  - `esports`, `map1..5`: победители отдельных карт (`BetScope.MAP_1..5`);
  - `maps_total`, `maps_handicap`: тоталы и форы карт (`StatType.MAPS`);
  - `rounds_total`, `rounds_handicap`: тоталы и форы раундов (`StatType.ROUNDS`).

## Testing Strategy
- Unit-тестирование `SbobetOddsMapperTest`:
  1. Основные рынки футбола (1X2, тоталы, форы, таймы);
  2. Роспись исходов (двойной шанс, обе забьют, ничья нет ставки);
  3. Статистические рынки (угловые, ЖК);
  4. Киберспортивные дисциплины (победители карт, тоталы карт и раундов);
  5. Граничные случаи (пустые рынки, null-значения, неподдерживаемые исходы).
