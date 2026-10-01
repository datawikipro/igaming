# Architecture Design: #40: [smarkets] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Smarkets — ведущая европейская биржа спортивных ставок (Betting Exchange), предоставляющая фид событий, рынков (markets), контрактов (contracts) и P2P стакана заявок (bids/offers quotes).
Для интеграции с ядром вилок и агрегации SmartBet.guru требуется переход от процедурного монолитного метода в `SmarketsOddsMapper` к расширяемой объектно-ориентированной модели обработчиков на основе Spring Component Discovery и `@Order`.

## Class Hierarchy & Design Pattern

```
                       ┌─────────────────────────┐
                       │  SmarketsMarketContext  │
                       └───────────┬─────────────┘
                                   │
                                   ▼
                      ┌─────────────────────────┐
                      │  SmarketsMarketHandler  │ (Interface)
                      └────────────▲────────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │ AbstractSmarketsMarketHandler│
                    └──────────────▲──────────────┘
                                   │
     ┌──────────────┬──────────────┼──────────────┬──────────────┬──────────────┐
     │              │              │              │              │              │
┌────┴─────┐ ┌──────┴──────┐ ┌─────┴─────┐ ┌──────┴──────┐ ┌─────┴─────┐ ┌──────┴──────┐
│MatchResul│ │TotalHandler │ │Handicap   │ │DoubleChance │ │DrawNoBet  │ │BttsHandler  │
└──────────┘ └─────────────┘ └───────────┘ └─────────────┘ └───────────┘ └─────────────┘
     │              │              │
┌────┴─────┐ ┌──────┴──────┐ ┌─────┴─────┐
│CorrectSco│ │StatsHandler │ │EsportsHand│
└──────────┘ └─────────────┘ └───────────┘
```

### 1. `SmarketsMarketContext`
Инкапсулирует контекст обрабатываемого рынка Smarkets:
- `event`: сущность события `SmarketsEvent` (id, name, type, startDatetime, state, slug)
- `market`: метаданные рынка `SmarketsMarket` (id, name, marketType with name and param)
- `contracts`: список контрактов для данного рынка `List<SmarketsContract>`
- `quotesByContractId`: ассоциативный массив лучших котировок `Map<String, SmarketsContractQuotes>`
- `sportType`: нормализованный тип спорта `SportType`
- `team1`, `team2`: имена соперников (парсинг "vs", "-", "@", "v")
- `isLive`: признак лайв-события

### 2. `SmarketsMarketHandler`
Интерфейс стратегии обработки:
- `boolean supports(SmarketsMarketContext context)`: проверка применимости обработчика к текущему рынку и типу спорта
- `void handle(SmarketsMarketContext context, List<OddItem> items)`: трансформация контрактов и котировок в канонические `OddItem`

### 3. `AbstractSmarketsMarketHandler`
Базовый класс с общей функциональностью:
- Извлечение лучших decimal odds для back-заявок (`quotes.getBestBackOdds()`)
- Парсинг числовых параметров тоталов и фор из `marketType.getParam()`, `market.getName()`, `contract.getName()`
- Определение `BetScope` (`FULL_MATCH`, `HALF_1`, `HALF_2`, `PERIOD_1..PERIOD_4`, `MAP_1..MAP_7`, `ROUND_1..ROUND_5`)
- Проверка дисциплин на киберспорт (`isEsports(SportType)`)
- Защита от нулевых и некорректных котировок (`addOddItem` с проверкой `value > 1.0` и валидным `BetType`)

### 4. Специализированные обработчики (`@Order`)
- `@Order(10) SmarketsCornersHandler`: Угловые удары (`StatType.CORNERS`)
- `@Order(20) SmarketsCardsHandler`: Карточки и ЖК (`StatType.YELLOW_CARDS`, `StatType.CARDS`)
- `@Order(30) SmarketsEsportsHandler`: Киберспорт (CS2, Dota 2, LoL, Valorant)
- `@Order(40) SmarketsPeriodHandler`: Таймы, периоды, четверти
- `@Order(50) SmarketsHalfTimeFullTimeHandler`: Рынки HT/FT
- `@Order(60) SmarketsMatchResultHandler`: 1X2, Moneyline, Full-Time Result
- `@Order(70) SmarketsDoubleChanceHandler`: Двойной шанс
- `@Order(80) SmarketsDrawNoBetHandler`: Ничья нет ставок
- `@Order(90) SmarketsTotalHandler`: Тоталы больше/меньше
- `@Order(100) SmarketsHandicapHandler`: Азиатские и европейские форы
- `@Order(110) SmarketsBttsHandler`: Обе забьют
- `@Order(120) SmarketsCorrectScoreHandler`: Точный счет

### 5. `SmarketsOddsMapper`
Координирующий фасад, внедряющий упорядоченный список `List<SmarketsMarketHandler>` через Spring Dependency Injection и оркестрирующий наполнение `OddsUpdateRequest`.
