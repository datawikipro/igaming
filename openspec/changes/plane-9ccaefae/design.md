# Architecture Design: [betano] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-betano` построен на Spring Boot 3.4.1 и библиотеке `igaming-source-core`.
Маппинг котировок переводится на объектно-ориентированную иерархию обработчиков:

### 1. `BetanoOddsMapper extends AbstractBetTypeMapper`:
- Поддержка `supports("betano", ...)`
- Поддержка регионов `BookmakerRegion.GLOBAL, BookmakerRegion.EU, BookmakerRegion.LATAM, BookmakerRegion.INT`
- Нормализация видов спорта через `SportNormalizationService`
- Итерация по специализированным обработчикам `BetanoMarketHandler`
- Формирование структурированного `OddsUpdateRequest`

### 2. Иерархия обработчиков рынков (`BetanoMarketHandler`):
- `AbstractBetanoMarketHandler`: базовый класс с утилитами нормализации скоупов (`FULL_MATCH`, `FIRST_HALF`, `SECOND_HALF`, `PERIOD_1..3`, `SET_1..5`, `MAP_1..5`), извлечения числовых параметров фор и тоталов, идентификации команд и форматирования групп.
- `BetanoMatchResultMarketHandler`: Moneyline, 1X2, 2-Way, 3-Way для матча и таймов.
- `BetanoDoubleChanceMarketHandler`: Двойной шанс (1X, 12, X2) для матча и таймов.
- `BetanoDrawNoBetMarketHandler`: Ничья исключена (Draw No Bet) для матча и таймов.
- `BetanoTotalMarketHandler`: Общие тоталы (Over/Under) и индивидуальные тоталы команд.
- `BetanoHandicapMarketHandler`: Европейские и азиатские форы/спреды для матча и таймов.
- `BetanoBothTeamsToScoreMarketHandler`: Обе забьют (BTTS: Yes/No) для матча и таймов.
- `BetanoCorrectScoreMarketHandler`: Точный счёт матча.
- `BetanoHalfTimeFullTimeMarketHandler`: Тайм / Матч (HT/FT: 1/1, 1/X, 1/2, X/1, X/X, X/2, 2/1, 2/X, 2/2).
- `BetanoPeriodMarketHandler`: Исходы по периодам, четвертям, сетам и таймам.
- `BetanoCornersMarketHandler`: Угловые (1X2, Over/Under тоталы, форы, двойной шанс, DNB, чёт/нечёт, первый/последний угловой).
- `BetanoCardsMarketHandler`: Жёлтые и суммарные карточки (1X2, тоталы, форы, первый/последний горчичник, удаление/красная карточка).
- `BetanoEsportsMarketHandler`: Киберспорт (победители карт Map 1..5, тоталы карт/раундов/убийств, форы по картам/раундам, First Blood).

### 3. Модели данных (DTO):
- `BetanoEventDto`: идентификатор события, названия команд, время начала, статус лайв, вид спорта, лига, список рынков.
- `BetanoMarketDto`: идентификатор рынка, наименование, описание, группа, тип периода, список исходов.
- `BetanoOutcomeDto`: идентификатор исхода, название, коэффициент (десятичный), фора/тотал/линия.
- `BetanoResponseDto`: контейнер ответа API.

### 4. Конфигурация и инфраструктура:
- `pom.xml`: интеграция в корень сборки, зависимости `spring-boot-starter-actuator`, `jib-maven-plugin` (3.4.1) с целевыми образами `100.78.183.101:30500/igaming-source-betano:latest` и `ghcr.io/datawikipro/igaming-source-betano:latest`.
- `application.properties`: неблокирующий HikariCP (`initialization-fail-timeout=0`), Actuator probes (`health,info`), PostgreSQL in-memory tmpfs URL, роутинг через кластерный HTTP-прокси `100.83.113.50:3128`.
- Модульные тесты `BetanoOddsMapperTest` с проверкой всех типов рынков.
