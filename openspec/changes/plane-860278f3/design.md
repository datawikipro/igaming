# Architecture Design: [wplay] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-wplay` осуществляет сбор линий и котировок колумбийского букмекера Wplay (платформа Playtech Sportsbook).
Цель рефакторинга — замена монолитной нормализации в `WplayOddsMapper` на компонентную ООП-архитектуру обработчиков рынков (`WplayMarketHandler`) на основе `AbstractBetTypeMapper` и паттерна Strategy / Chain of Responsibility с поддержкой контекста вида спорта (`SportType`), расширенной росписи исходов, статистических маркетов (угловые, ЖК) и киберспорта (CS2, Dota 2, League of Legends, Valorant).

## Component Structure

```
pro.datawiki.igaming.source.wplay
├── dto
│   ├── WplayEventDto
│   ├── WplayMarketDto
│   ├── WplayOutcomeDto
│   └── WplayResponseDto
├── service
│   ├── WplayOddsMapper (extends AbstractBetTypeMapper, chains WplayMarketHandler)
│   └── handler
│       ├── WplayMarketHandler (интерфейс обработчика маркета)
│       ├── AbstractWplayMarketHandler (базовый класс с хелперами resolveScope, extractNumber, addOddItem)
│       ├── MatchResultMarketHandler (1X2, Moneyline, Full Time Result)
│       ├── DoubleChanceMarketHandler (1X, 12, X2 матча и периодов)
│       ├── DrawNoBetMarketHandler (Draw No Bet -> Handicap 0.0)
│       ├── BothTeamsToScoreMarketHandler (BTTS Yes/No, Both Halves)
│       ├── TotalMarketHandler (Over/Under общие и индивидуальные тоталы команд)
│       ├── HandicapMarketHandler (Asian Handicap, Point Spread)
│       ├── CorrectScoreMarketHandler (Точный счет)
│       ├── HalfTimeFullTimeMarketHandler (Тайм/Матч HT/FT)
│       ├── PeriodMarketHandler (1st Half, 2nd Half, периоды, четверти, сеты)
│       ├── CornersMarketHandler (Угловые: 1X2, тоталы, форы с StatType.CORNERS)
│       ├── CardsMarketHandler (ЖК: 1X2, тоталы, форы, Red Card с StatType.YELLOW_CARDS)
│       └── EsportsMarketHandler (CS2, Dota 2, LoL, Valorant: карты, раунды, убийства, First Blood)
```

## Feed Protocol & Data Mapping
События Wplay представлены DTO-структурами:
- `WplayEventDto`: идентификатор события, названия команд, спорт, лига, статус live, список маркетов.
- `WplayMarketDto`: название рынка (`name`), категория, список исходов.
  - "1X2", "Match Winner", "Resultado del partido", "Ganador" -> `MatchResultMarketHandler`
  - "Double Chance", "Doble oportunidad" -> `DoubleChanceMarketHandler`
  - "Draw No Bet", "Apuesta sin empate", "Empate no acción" -> `DrawNoBetMarketHandler`
  - "Both Teams To Score", "Ambos equipos marcarán", "BTTS" -> `BothTeamsToScoreMarketHandler`
  - "Total Goals", "Total", "Over/Under", "Más de / Menos de" -> `TotalMarketHandler`
  - "Handicap", "Asian Handicap", "Hándicap" -> `HandicapMarketHandler`
  - "Correct Score", "Marcador exacto" -> `CorrectScoreMarketHandler`
  - "Half Time / Full Time", "Descanso / Final", "HT/FT" -> `HalfTimeFullTimeMarketHandler`
  - "1st Half", "2nd Half", "Primer tiempo" -> `PeriodMarketHandler`
  - "Corners", "Tiros de esquina", "Córners" -> `CornersMarketHandler`
  - "Cards", "Tarjetas amarillas", "Yellow Cards" -> `CardsMarketHandler`
  - "Map 1 Winner", "Total Rounds", "First Blood" -> `EsportsMarketHandler`

## Testing Strategy
- Разработка unit-тестов в `pro.datawiki.igaming.source.wplay.service.WplayOddsMapperTest`:
  1. Основные рынки: 1X2, Moneyline, тоталы, форы;
  2. Роспись исходов: двойной шанс, обе забьют, ничья исключена, точный счет, HT/FT;
  3. Статистические рынки: угловые (1X2, тотал, фора), желтые карточки (тотал, удаление);
  4. Киберспортивные дисциплины: победа по картам (MAP_1..5), фора карт, тотал раундов, First Blood, тотал убийств;
  5. Обратная совместимость: тестирование HTML-парсинга `mapHtmlToOddsUpdateRequest` и `supports("wplay", ...)`.
