# Design: [fanduel] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-fanduel` построен на Spring Boot 3.4 и `igaming-source-core`.
Маппинг котировок переводится на объектно-ориентированную иерархию обработчиков:
1. `FanDuelOddsMapper extends AbstractBetTypeMapper`:
   - Поддержка `supports("fanduel", ...)`
   - Инициализация регионов `BookmakerRegion.US, BookmakerRegion.GLOBAL`
   - Детерминированная генерация `factorId`
   - Точная конвертация американских коэффициентов в десятичные
   - Проход по цепочке специализированных обработчиков `FanDuelMarketHandler`
   - Fallback к `betTypeResolver` и регистрация в `unmappedBetService`

2. Обработчики рынков (`FanDuelMarketHandler`):
   - `FanDuelResultMarketHandler`: Moneyline, Match Result, 1X2, 2-Way, 3-Way, Double Chance (1X, 12, X2), Draw No Bet (WIN1_2WAY, WIN2_2WAY) для всех периодов и статистических маркетов.
   - `FanDuelTotalMarketHandler`: тоталы (Over/Under), тоталы команд (TEAM1, TEAM2), периоды, азиатские и европейские значения.
   - `FanDuelHandicapMarketHandler`: спреды, форы (Spread, Point Spread, Handicap, Run Line, Puck Line) для матча, таймов, четвертей, периодов.
   - `FanDuelStatsMarketHandler`: статистика футбола и игровых видов: угловые (`StatType.CORNERS`), жёлтые карточки (`StatType.YELLOW_CARDS`), карточки (`StatType.CARDS`), фолы (`StatType.FOULS`), офсайды (`StatType.OFFSIDES`), удары в створ (`StatType.SHOTS_ON_TARGET`), удары в створ ворот (`StatType.SHOTS_ON_GOAL`).
   - `FanDuelEsportsMarketHandler`: киберспортивные рынки: победители карт (`MAP_1`, `MAP_2`, ...), тотал карт (`StatType.MAPS`), тотал раундов (`StatType.ROUNDS`), форы по картам/раундам, тотал убийств (`StatType.KILLS`), первая кровь (`BinaryMarketBet.MarketType.FIRST_BLOOD`), башни (`StatType.TOWERS`), рошан/барон (`StatType.ROSHAN`, `StatType.BARON`).
   - `FanDuelPropsMarketHandler`: роспись и спецмаркеты: обе забьют (`BinaryMarketBet.MarketType.BTTS`), чёт/нечёт (`BinaryMarketBet.MarketType.ODD_EVEN`), точный счёт (`CorrectScoreBet`).

3. Вспомогательные резолверы:
   - `FanDuelScopeResolver`: нормализация `BetScope` (FULL_MATCH, FIRST_HALF, SECOND_HALF, PERIOD_1..3, QUARTER_1..4, INNING_1, FIRST_5_INNINGS, SET_1..5, MAP_1..5).
   - `FanDuelStatTypeResolver`: нормализация `StatType`.

4. Инфраструктура:
   - Добавление `spring-boot-starter-actuator` в `pom.xml`.
   - Добавление `jib-maven-plugin` в `pom.xml` с целевым образом `100.78.183.101:30500/igaming-source-fanduel:latest`.
   - Настройка неблокирующего HikariCP и Actuator probes в `application.properties`.
   - Комплексные модульные тесты `FanDuelOddsMapperTest`.
