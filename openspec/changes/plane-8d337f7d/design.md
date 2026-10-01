# Design: [888sport] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Class Diagram

```mermaid
classDiagram
    direction TB
    class Sport888OddsMapper {
        -List~Sport888MarketHandler~ marketHandlers
        -UnmappedBetService unmappedBetService
        -SportNormalizationService sportNormalizationService
        -BetTypeResolverService betTypeResolver
        +mapToOddsUpdateRequest(response, sport, league) OddsUpdateRequest
    }

    class Sport888MarketHandler {
        <<interface>>
        +supports(betOffer, marketName, sportType) boolean
        +handle(event, betOffer, sportType, marketName, items) void
    }

    class AbstractSport888MarketHandler {
        <<abstract>>
        #extractDecimalOdds(outcome) Double
        #addOddItem(items, outcome, groupName, rawOutcomeName, value, betType) void
        #resolveScope(marketName) BetScope
        #determineTotalSubject(marketDesc, team1, team2) BetSubject
        +isStats(marketName)$ boolean
        +isEsports(sportType)$ boolean
    }

    class Sport888StatsHandler {
        +supports()
        +handle()
    }
    class Sport888EsportsHandler {
        +supports()
        +handle()
    }
    class Sport888MatchResultHandler {
        +supports()
        +handle()
    }
    class Sport888TotalHandler {
        +supports()
        +handle()
    }
    class Sport888HandicapHandler {
        +supports()
        +handle()
    }
    class Sport888DoubleChanceHandler {
        +supports()
        +handle()
    }
    class Sport888DrawNoBetHandler {
        +supports()
        +handle()
    }
    class Sport888BttsHandler {
        +supports()
        +handle()
    }
    class Sport888CorrectScoreHandler {
        +supports()
        +handle()
    }

    Sport888MarketHandler <|.. AbstractSport888MarketHandler
    AbstractSport888MarketHandler <|-- Sport888StatsHandler
    AbstractSport888MarketHandler <|-- Sport888EsportsHandler
    AbstractSport888MarketHandler <|-- Sport888MatchResultHandler
    AbstractSport888MarketHandler <|-- Sport888TotalHandler
    AbstractSport888MarketHandler <|-- Sport888HandicapHandler
    AbstractSport888MarketHandler <|-- Sport888DoubleChanceHandler
    AbstractSport888MarketHandler <|-- Sport888DrawNoBetHandler
    AbstractSport888MarketHandler <|-- Sport888BttsHandler
    AbstractSport888MarketHandler <|-- Sport888CorrectScoreHandler
    Sport888OddsMapper o-- Sport888MarketHandler
```

## Order of Handlers Execution
1. `@Order(10) Sport888StatsHandler`: Угловые (Corners), Карточки (Yellow Cards/Cards).
2. `@Order(20) Sport888EsportsHandler`: Карты (Maps), Раунды (Rounds), Киллы (Kills), First Blood для киберспорта.
3. `@Order(30) Sport888DoubleChanceHandler`: 1X, 12, X2.
4. `@Order(40) Sport888BttsHandler`: Обе забьют (YES/NO).
5. `@Order(50) Sport888DrawNoBetHandler`: Ничья исключена (WIN1_2WAY, WIN2_2WAY).
6. `@Order(60) Sport888CorrectScoreHandler`: Точный счет и Any Other Score.
7. `@Order(70) Sport888TotalHandler`: Тоталы матча, командные тоталы, периоды/таймы.
8. `@Order(80) Sport888HandicapHandler`: Форы и спреды.
9. `@Order(90) Sport888MatchResultHandler`: 1X2, Moneyline, победитель матча/тайма.
10. Fallback: `BetTypeResolverService` и аудит в `UnmappedBetService`.
