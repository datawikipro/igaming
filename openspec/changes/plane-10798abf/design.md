# Architecture Design: [atg] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-atg` осуществляет сбор линий и котировок шведского букмекера ATG, работающего на платформе Kambi.
Цель рефакторинга — переход к модульной ООП-архитектуре обработчиков рынков (`AtgMarketHandler`) с поддержкой расширенной росписи исходов, статистических маркетов (угловые, ЖК) и киберспортивных дисциплин (CS2, Dota 2, League of Legends, Valorant и др.).

## Component Structure

```
pro.datawiki.igaming.source.atg.service
├── AtgOddsMapper (extends AbstractBetTypeMapper, iterates marketHandlers with SportType)
└── handler
    ├── AtgMarketHandler (интерфейс с поддержкой supports/handle/handleOffer для KambiBetOffer, marketName и SportType)
    ├── AbstractAtgMarketHandler (базовый абстрактный класс с хелперами resolveScope, extractDecimalOdds, addOddItem, addMatchResult, addTotal, addHandicap, addBinary)
    ├── AtgMoneylineHandler (1X2, Moneyline, исходы таймов/периодов)
    ├── AtgTotalHandler (Тоталы Over/Under матча, таймов и команд)
    ├── AtgHandicapHandler (Форы матча и таймов, European & Asian Handicap)
    ├── AtgDoubleChanceHandler (Двойной шанс: 1X, 12, X2 матча и 1-го тайма)
    ├── AtgBttsHandler (Обе забьют: Yes/No матча и 1-го тайма)
    ├── AtgDrawNoBetHandler (Ничья нет ставки / Фора 0.0 матча и 1-го тайма)
    ├── AbstractAtgStatsHandler (базовый класс для статистических маркетов)
    ├── AtgStatsCornersHandler (Угловые: 1X2, тоталы, форы с StatType.CORNERS)
    ├── AtgStatsCardsHandler (ЖК/предупреждения: 1X2, тоталы, форы с StatType.YELLOW_CARDS)
    └── AtgEsportsHandler (Киберспорт: победители карт MAP_1..5, тотал/фора карт StatType.MAPS, раунды StatType.ROUNDS)
```

## Market Handlers & Protocols
- **Основные рынки**: 1X2, Moneyline (2-way / 3-way), тоталы Over/Under и форы матча и таймов/периодов.
- **Роспись исходов**:
  - `double_chance`, `double_chance_half1`: 1X (`DC_1X`), 12 (`DC_12`), X2 (`DC_X2`) исходы;
  - `btts`, `both_teams_to_score`: BTTS Yes/No;
  - `draw_no_bet`, `dnb`: исходы без ничьей (Handicap 0.0).
- **Статистические маркеты**:
  - `corners*`: угловые с `StatType.CORNERS` (исходы 1X2, тоталы Over/Under, форы);
  - `cards*`, `yellow_cards*`: желтые карточки с `StatType.YELLOW_CARDS`.
- **Киберспорт**:
  - `esports`, `map1..5`: победители отдельных карт (`BetScope.MAP_1..5`);
  - `maps_total`, `maps_handicap`: тоталы и форы карт (`StatType.MAPS`);
  - `rounds_total`, `rounds_handicap`: тоталы и форы раундов (`StatType.ROUNDS`).

## Testing Strategy
- Unit-тестирование `AtgOddsMapperTest`:
  1. Основные рынки футбола (1X2, тоталы, форы, таймы);
  2. Роспись исходов (двойной шанс, обе забьют, ничья нет ставки);
  3. Статистические рынки (угловые, ЖК);
  4. Киберспортивные дисциплины (победители карт, тоталы карт и раундов);
  5. Граничные случаи (пустые предложения, null-значения, неподдерживаемые исходы).
