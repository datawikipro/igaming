# Proposal: [caliente] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `f00bf165-282f-4cf7-b242-9e978a70e179`
- **Bookmaker**: Caliente (Mexico #1, Playtech platform / `sports.caliente.mx`)
- **Target Module**: `igaming-source-caliente`

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-caliente`:
1. **Переход к модульной архитектуре обработчиков рынков**: создание иерархии обработчиков `CalienteMarketHandler` на базе паттерна Strategy / Handler и наследования `AbstractBetTypeMapper`.
2. **DTO-модель данных**: добавление типизированных DTO (`CalienteEventDto`, `CalienteMarketDto`, `CalienteOutcomeDto`, `CalienteResponseDto`) для структурированного представления рынков и исходов.
3. **Киберспорт (`EsportsMarketHandler`)**: поддержка дисциплин CS2, Dota 2, League of Legends, Valorant (победители матча/карт `MAP_1`..`MAP_5`, тоталы и форы карт `StatType.MAPS`, тоталы и форы раундов `StatType.ROUNDS`, First Blood `BinaryMarketBet`).
4. **Статистика**:
   - Угловые удары (`CornersMarketHandler` - `StatType.CORNERS`): тоталы (общие/индивидуальные), форы, 1X2, первый/последний угловой, двойной шанс, DNB.
   - Желтые карточки (`CardsMarketHandler` - `StatType.YELLOW_CARDS`): тоталы, форы, 1X2, красная карточка (удаление Yes/No).
5. **Основная и расширенная роспись исходов**:
   - `MatchResultMarketHandler` (1X2, Moneyline, Full Time Result);
   - `DoubleChanceMarketHandler` (1X, 12, X2 матча и периодов);
   - `DrawNoBetMarketHandler` (Draw No Bet -> Handicap 0.0);
   - `BothTeamsToScoreMarketHandler` (BTTS Yes/No, Both Halves);
   - `TotalMarketHandler` (Over/Under общие и индивидуальные тоталы команд);
   - `HandicapMarketHandler` (Asian Handicap, Point Spread);
   - `CorrectScoreMarketHandler` (Точный счет);
   - `HalfTimeFullTimeMarketHandler` (Тайм/Матч HT/FT);
   - `PeriodMarketHandler` (1st Half, 2nd Half, периоды, четверти, сеты).
6. **Интеграция в `CalienteOddsMapper`**: поддержка передачи DTO-событий, полная обратная совместимость с HTML-парсингом `mapHtmlToOddsUpdateRequest` и `supports("caliente", ...)`.
7. **Тестирование**: всестороннее покрытие unit-тестами в `CalienteOddsMapperTest`.
