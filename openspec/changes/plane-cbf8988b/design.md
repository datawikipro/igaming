# Architecture Design: [bwin] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-bwin` осуществляет сбор линий и котировок международного букмекера Bwin, функционирующего на базе платформы Entain (CDS API / Asyncdsl).
Цель рефакторинга — замена монолитного метода нормализации рынков в `BwinOddsMapper` на компонентную ООП-архитектуру обработчиков рынков (`BwinMarketHandler`) на основе `AbstractBetTypeMapper` и паттерна Chain of Responsibility / Strategy с поддержкой контекста вида спорта (`SportType`), расширенной росписи исходов, статистических маркетов (угловые, ЖК) и киберспорта (CS2, Dota 2, League of Legends, Valorant).

## Component Structure

```
pro.datawiki.igaming.source.bwin.service
├── BwinOddsMapper (extends AbstractBetTypeMapper, iterates marketHandlers with SportType fallback)
└── handler
    ├── BwinMarketHandler (интерфейс обработчика маркета Entain)
    ├── AbstractBwinMarketHandler (базовый абстрактный класс с хелперами resolveScope, extractNumber, addOddItem)
    ├── BwinMatchResultHandler (1X2, Moneyline, исходы таймов/периодов)
    ├── BwinDoubleChanceHandler (Двойной шанс матча и таймов: 1X, 12, X2)
    ├── BwinDrawNoBetHandler (Ничья исключена -> Handicap 0.0)
    ├── BwinTotalHandler (Тоталы матча, индивидуальные тоталы, тоталы таймов)
    ├── BwinHandicapHandler (Форы матча и таймов, European & Asian Handicap)
    ├── BwinBttsHandler (Обе забьют матча и таймов: Yes / No)
    ├── BwinCorrectScoreHandler (Точный счет матча и таймов)
    ├── AbstractBwinStatsHandler (базовый класс для статистических маркетов)
    ├── BwinStatsCornersHandler (Угловые: исходы 1X2, тоталы, форы с StatType.CORNERS)
    ├── BwinStatsCardsHandler (ЖК: исходы 1X2, тоталы, форы с StatType.YELLOW_CARDS)
    └── BwinEsportsHandler (Киберспорт: победители карт MAP_1..5, тотал/фора карт StatType.MAPS, раунды StatType.ROUNDS, First Blood)
```

## Feed Protocol & Data Mapping
Взаимодействие с платформой Entain осуществляется через CDS API:
- `EntainFixture` содержит `optionMarkets` со списком `EntainOptionMarket`.
- Каждый `EntainOptionMarket` имеет наименование маркета (`name.value`), например:
  - "Match Result", "1X2", "Winner" -> `BwinMatchResultHandler`
  - "Double Chance" -> `BwinDoubleChanceHandler`
  - "Draw No Bet" -> `BwinDrawNoBetHandler`
  - "Total Goals", "Goals Over/Under", "Totals" -> `BwinTotalHandler`
  - "Handicap", "Spread", "Asian Handicap" -> `BwinHandicapHandler`
  - "Both Teams To Score", "BTTS" -> `BwinBttsHandler`
  - "Correct Score" -> `BwinCorrectScoreHandler`
  - "Corners - Match Total", "Corners - Handicap", "Corners - 1X2" -> `BwinStatsCornersHandler`
  - "Yellow Cards - Match Total", "Yellow Cards - 1X2", "Red Card" -> `BwinStatsCardsHandler`
  - "Map 1 Winner", "Map Handicap", "Total Maps", "Round Handicap", "First Blood" -> `BwinEsportsHandler`

## Testing Strategy
- Разработка unit-тестов в `pro.datawiki.igaming.source.bwin.service.BwinOddsMapperTest`:
  1. Основные рынки: 1X2, Moneyline, таймы/периоды, тоталы, форы;
  2. Роспись исходов: двойной шанс, обе забьют, ничья исключена, точный счет;
  3. Статистические рынки: угловые (1X2, тотал, фора), желтые карточки и удаление;
  4. Киберспортивные дисциплины: победа по картам (MAP_1..5), фора карт, тотал карт, тотал раундов, First Blood;
  5. Граничные случаи: пустые и некорректные группы, null-значения, коэффициенты <= 1.0, безопасный fallback.
