# Architecture Design: #37: [bcgame] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
BC.Game — ведущий криптовалютный букмекер с высоконагруженным фидом спортивных и киберспортивных событий. Для интеграции требуется переход от монолитного процедурного маппинга к расширяемой объектно-ориентированной модели обработчиков на основе Spring Component Discovery и `@Order`.

## Class Hierarchy & Design Pattern

```
                       ┌──────────────────────┐
                       │ BcgameMarketContext  │
                       └──────────┬───────────┘
                                  │
                                  ▼
                     ┌─────────────────────────┐
                     │   BcgameMarketHandler   │ (Interface)
                     └────────────▲────────────┘
                                  │
                   ┌──────────────┴──────────────┐
                   │ AbstractBcgameMarketHandler │
                   └──────────────▲──────────────┘
                                  │
       ┌──────────────┬───────────┼───────────┬──────────────┬──────────────┐
       │              │           │           │              │              │
┌──────┴──────┐ ┌─────┴─────┐ ┌───┴────┐ ┌────┴─────┐ ┌──────┴──────┐ ┌─────┴─────┐
│ResultHandler│ │TotalHandl.│ │Handicap│ │BttsHandl.│ │StatsHandler│ │EsportsHand│
└─────────────┘ └───────────┘ └────────┘ └──────────┘ └─────────────┘ └───────────┘
```

### 1. `BcgameMarketContext`
Инкапсулирует контекст обрабатываемого события:
- `eventId`: идентификатор события
- `sportName`: исходное имя спорта
- `sportType`: нормализованный тип спорта (`SportType`)
- `leagueName`: лига/турнир
- `homeTeam`, `awayTeam`: названия команд
- `isLive`: статус лайв-матча

### 2. `BcgameMarketHandler`
Интерфейс стратегии обработки:
- `boolean supports(String marketName, BcgameMarketContext context)`
- `void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items)`

### 3. `AbstractBcgameMarketHandler`
Базовый класс с общей функциональностью:
- Извлечение числовых параметров (тоталы, форы, гандикапы)
- Определение `BetScope` (FULL_MATCH, HALF_1, HALF_2, MAP_1..MAP_7, ROUND_1..ROUND_5)
- Проверка дисциплины на киберспорт (`isEsports(sportType)`)
- Валидация коэффициентов и безопасное добавление `OddItem`

### 4. Специализированные обработчики (`@Order`)
- `@Order(10) BcgameStatsMarketHandler`: Статистика (угловые, ЖК, карточки, фолы, офсайды, удары)
- `@Order(20) BcgameEsportsMarketHandler`: Киберспорт (победители карт, тоталы/форы карт, раундов, киллов, First Blood)
- `@Order(30) BcgameResultMarketHandler`: 1X2, Moneyline, Double Chance, Draw No Bet
- `@Order(40) BcgameTotalMarketHandler`: Тоталы матча, таймов и индивидуальные тоталы
- `@Order(50) BcgameHandicapMarketHandler`: Форы матча и периодов
- `@Order(60) BcgameBttsHandler`: Обе забьют
- `@Order(70) BcgameCorrectScoreHandler`: Точный счет

### 5. `BcgameOddsMapper`
Координирующий компонент, принимающий `List<BcgameMarketHandler> handlers` и инкапсулирующий вызов обработчиков для каждого рынка события.
