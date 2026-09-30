# Architecture Design: [sbobet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Class Diagram

```mermaid
classDiagram
    direction TB
    class SbobetOddsMapper {
        -SportNormalizationService sportNormalizationService
        -List~SbobetMarketHandler~ marketHandlers
        +mapToOddsUpdateRequest(event, sportName, sportType, leagueName) OddsUpdateRequest
    }

    class SbobetMarketHandler {
        <<interface>>
        +supports(marketKey) boolean
        +supports(marketKey, sportType) boolean
        +handle(marketNode, items) void
        +handle(marketNode, sportType, items) void
    }

    class AbstractSbobetMarketHandler {
        <<abstract>>
        #addOddItem(items, groupName, rawOutcomeName, value, betType) void
        #resolveScope(marketKey, node) BetScope
        +isStats(marketKey)$ boolean
        +isEsports(sportType)$ boolean
    }

    class SbobetStatsHandler {
        +supports()
        +handle()
    }
    class SbobetEsportsHandler {
        +supports()
        +handle()
    }
    class SbobetDoubleChanceHandler {
        +supports()
        +handle()
    }
    class SbobetDrawNoBetHandler {
        +supports()
        +handle()
    }
    class SbobetBttsHandler {
        +supports()
        +handle()
    }
    class SbobetCorrectScoreHandler {
        +supports()
        +handle()
    }
    class SbobetTotalHandler {
        +supports()
        +handle()
    }
    class SbobetHandicapHandler {
        +supports()
        +handle()
    }
    class SbobetMoneylineHandler {
        +supports()
        +handle()
    }

    SbobetMarketHandler <|.. AbstractSbobetMarketHandler
    AbstractSbobetMarketHandler <|-- SbobetStatsHandler
    AbstractSbobetMarketHandler <|-- SbobetEsportsHandler
    AbstractSbobetMarketHandler <|-- SbobetDoubleChanceHandler
    AbstractSbobetMarketHandler <|-- SbobetDrawNoBetHandler
    AbstractSbobetMarketHandler <|-- SbobetBttsHandler
    AbstractSbobetMarketHandler <|-- SbobetCorrectScoreHandler
    AbstractSbobetMarketHandler <|-- SbobetTotalHandler
    AbstractSbobetMarketHandler <|-- SbobetHandicapHandler
    AbstractSbobetMarketHandler <|-- SbobetMoneylineHandler
    SbobetOddsMapper o-- SbobetMarketHandler
```

## Order of Handlers Execution
1. `@Order(10) SbobetStatsHandler`: Угловые (Corners), Карточки (Yellow Cards/Cards).
2. `@Order(20) SbobetEsportsHandler`: Карты (Maps), Раунды (Rounds), Киллы (Kills), First Blood для киберспорта.
3. `@Order(30) SbobetDoubleChanceHandler`: 1X, 12, X2 (Match and Half-1).
4. `@Order(40) SbobetDrawNoBetHandler`: Ничья исключена (WIN1_2WAY, WIN2_2WAY).
5. `@Order(50) SbobetBttsHandler`: Обе забьют (YES/NO).
6. `@Order(60) SbobetCorrectScoreHandler`: Точный счет и Any Other Score.
7. `@Order(70) SbobetTotalHandler`: Тоталы матча и таймов.
8. `@Order(80) SbobetHandicapHandler`: Форы и спреды матча и таймов.
9. `@Order(90) SbobetMoneylineHandler`: 1X2 и Moneyline матча и таймов.
