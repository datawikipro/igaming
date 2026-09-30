# Architecture Design: #11: [marathonbet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context & Architecture
Модуль `igaming-source-marathonbet` построен на Spring Boot 3.4.1 и библиотеке `igaming-source-core`.
Маппинг котировок выполняется через компоненты `AbstractBetTypeMapper`, которые автоматически регистрируются в Spring ApplicationContext и опрашиваются сервисом `BetTypeResolverService`.

### 1. Архитектура мапперов
- **Киберспорт (`mapper.esports`)**:
  - `MarathonEsportsResultMapper`: исходы матча (WIN1, WIN2, DRAW, 2-way) и победа на конкретной карте (`MAP_1` .. `MAP_5`).
  - `MarathonEsportsTotalMapper`: тоталы карт (`StatType.MAPS`), раундов (`StatType.ROUNDS`), фрагов (`StatType.KILLS`) для матча и отдельных карт.
  - `MarathonEsportsHandicapMapper`: форы по картам (`StatType.MAPS`), раундам (`StatType.ROUNDS`) и фрагам (`StatType.KILLS`) с корректным сохранением параметра форы (`param`).
- **Футбольная статистика (`mapper.football`)**:
  - `MarathonFootballStatsMapper`: универсальный обработчик статистических маркеров `StatType.CORNERS`, `StatType.YELLOW_CARDS`, `StatType.FOULS`, `StatType.OFFSIDES`, `StatType.SHOTS_ON_TARGET`.
  - Поддержка исходов 1X2, двойного шанса (`DC_1X`, `DC_12`, `DC_X2`), тоталов (общих, индивидуальных, азиатских, чет/нечет) и фор для матча и тайма (`HALF_1`, `HALF_2`).
- **Роспись исходов и спецмаркеты (`mapper.football`)**:
  - `MarathonFootballSpecialMapper`: BTTS (`BinaryMarketBet.MarketType.BTTS`), `TO_SCORE_IN_BOTH_HALVES`, `TeamToScoreBet`, `HalfTimeFullTimeBet` (HT/FT 9 исходов), `CORRECT_SCORE`, красные карточки и пенальти.
- **Очистка фильтров подавления (`service.MarathonOddsMapper`)**:
  - Деактивация блокирующих регулярных выражений в `SUPPRESS_M_PATTERN` и `SUPPRESS_O_PATTERN`, препятствовавших попаданию росписи, тайм-маркетов и исходов карт в мапперы.

### 2. Сборка и деплой (Verification & Deployment Strategy)
- Jib-плагин версии 3.4.1 для сборки OCI-образа `ghcr.io/datawikipro/igaming-source-marathonbet:latest`.
- Деплой тестового пода `igaming-source-marathonbet-test` в K8s namespace `igaming-dev`.
- 5-минутный soak-тест (`schedule`) без единого Exception/NPE в логах.
- Слияние ветки в master, деплой в прод (`igaming-source`) и финальный 5-минутный мониторинг линии (порог >= 500 матчей).
