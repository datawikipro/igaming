# Architecture Design: #10: [fon-bet-ru] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-fon-bet-ru` построен на Spring Boot 3.4 и базовых компонентах `igaming-source-core`.
Маппинг рынков и исходов реализован через декомпозицию и строгую иерархию компонентов `RuleBasedBetTypeMapper`:
- **Общие исходы, тоталы, форы и спецмаркеты**: `FonbetCommonResultMapper`, `FonbetCommonTotalMapper`, `FonbetCommonHandicapMapper`, `FonbetCommonSpecialMapper`.
  - Обрабатывают основные маркеты матча (`stat == StatType.MATCH`).
  - Исключают киберспортивные виды спорта (`FonbetEsportsMapper.isEsportsSport(sportType)`), делегируя их специализированному мапперу.
- **Киберспортивные рынки**: `FonbetEsportsMapper`.
  - Обслуживает `SportType.ESPORTS`, `CS2`, `DOTA2`, `LEAGUE_OF_LEGENDS`, `VALORANT`, `STARCRAFT`, киберспорт-симуляторы.
  - Поддерживает победителей матча и карт (с распознаванием BetScope `MAP_1`..`MAP_5`), тоталы и форы по картам (`StatType.MAPS`), раундам (`StatType.ROUNDS`), фрагам/убийствам (`StatType.KILLS`), а также спецсобытия (First Blood, Towers, Roshan, Baron).
- **Статистические маркеты**: `FonbetStatsMapper`.
  - Обслуживает статистические типы (`StatType.CORNERS`, `CARDS`, `YELLOW_CARDS`, `FOULS`, `OFFSIDES`, `SHOTS_ON_TARGET`, `PENALTY_MINUTES`).
  - Предотвращает коллизии с общими тоталами и форами благодаря изоляции статистических исходов.
- **Предотвращение коллизий**: строгое правило единственного соответствия в `BetTypeResolverService`, исключающее дублирование ключей и потерю котировок.

## Verification & Deployment Strategy
- **Сборка OCI-образа через Maven Jib**: `100.78.183.101:30500/igaming-source-fon-bet-ru:latest`.
- **Запуск тестового пода в K8s**: неблокирующий HikariCP, Actuator probes `/actuator/health/readiness` и `/actuator/health/liveness`.
- **5-минутный тест тестового пода (Soak Test)**: мониторинг логов без Exception/FATAL/OOM/коллизий.
- **Мердж в master**: оформление изменений и слияние в основную ветку.
- **Деплой в прод**: обновление подов `igaming-source-fon-bet-ru-crawler` и `igaming-source-fon-bet-ru-loader` в namespace `igaming-source`.
- **5-минутный мониторинг прода**: аудит логов и проверка наполнения линии (`SELECT count(*) FROM match_cache >= 500`).
