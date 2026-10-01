# Design: #39: [paf] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration

Модуль `igaming-source-paf` спроектирован как высокопроизводительный микросервис сбора данных, основанный на Spring Boot 3.4 (Java 21) и архитектурном фреймворке `igaming-source-core`.

### Компоненты системы:
1. **`PafApiClient`**:
   - HTTP-клиент на базе Apache HttpClient 5 и `RestTemplate`.
   - Подключение через выделенный кластерный HTTP-прокси (`http://100.83.113.50:3128`).
   - Запросы к Kambi Offering API:
     - `GET /offering/v2018/paf/listView/{sportSlug}.json?lang={locale}&market={market}` — получение активных событий по спорту (locale: `fi_FI`, market: `FI`).
     - `GET /offering/v2018/paf/betoffer/event/{eventId}.json?lang={locale}&market={market}` — детальная роспись исходов события.
   - Заголовки: эмуляция реального браузера с Origin `https://www.paf.com` и Referer `https://www.paf.com/`.

2. **`PafOddsMapper` & Handlers (`pro.datawiki.igaming.source.paf.service.handler`)**:
   - Наследует `AbstractKambiOddsMapper` из `igaming-source-core`.
   - Имя букмекера: `paf`.
   - Регионы: `GLOBAL`, `EU`, `FI`, `SE`, `EE`, `ES`.
   - Маппинг рынков через упорядоченные ООП-стратегии (`PafMarketHandler`):
     - `PafEsportsHandler` (`@Order(10)`): киберспортивные маркеты (карта 1/2/3, победитель карты/матча в CS2, Dota 2, LoL, Valorant, тотал раундов, форы, First Blood).
     - `PafStatsHandler` (`@Order(20)`): статистика угловых и желтых карточек (тоталы матча и команд, форы, 1X2 исход, таймы).
     - `PafCorrectScoreHandler` (`@Order(30)`): точный счет.
     - `PafBttsHandler` (`@Order(40)`): обе забьют (BTTS: Yes/No).
     - `PafDrawNoBetHandler` (`@Order(50)`): ничья нет ставок (Draw No Bet -> Handicap 0.0).
     - `PafDoubleChanceHandler` (`@Order(60)`): двойной шанс (1X, 12, X2).
     - `PafHandicapHandler` (`@Order(70)`): форы европейские и азиатские.
     - `PafTotalHandler` (`@Order(80)`): тоталы Over/Under матча и индивидуальные тоталы.
     - `PafMatchResultHandler` (`@Order(90)`): денежная линия (1X2, Moneyline).

3. **`PafDiscoveryService`**:
   - Сканирование 16 видов спорта (`football`, `tennis`, `basketball`, `ice_hockey`, `baseball`, `esports`, `volleyball`, `handball`, `table_tennis`, `cricket`, `golf`, `boxing`, `motorsports`, `american_football`, `darts`, `snooker`).
   - Дедупликация и сохранение в локальный кэш `match_cache` через `MatchPersistenceService`.
   - Защита от переполнения кэша (LRU/очистка при достижении порога).

4. **`MatchService` & `MatchFetchScheduler`**:
   - Наследование от `AbstractBaseBookmakerService` (`getBookmakerFamily() = "paf"`).
   - Формирование `OddsUpdateRequest` и отправка котировок в `AggregatorClient` (`http://igaming-aggregator.igaming-dev.svc.cluster.local:8080`).
   - Периодический опрос линий и отправка Heartbeat каждые 60 секунд.

5. **K8s & Database Architecture**:
   - Сборка OCI-образа через Jib: `100.78.183.101:30500/igaming-source-paf:latest`.
   - Неблокирующий HikariCP (`initialization-fail-timeout=0`, `connection-timeout=5000`).
   - Health probes Actuator: `/actuator/health/liveness`, `/actuator/health/readiness`.
   - PostgreSQL 15/16 StatefulSet в namespace `igaming-source` (`igaming-source-paf-db`) с параметрами `synchronous_commit=off`, `fsync=off` в `emptyDir` tmpfs.

## Verification & Deployment Strategy
- **Unit-тестирование**: `PafOddsMapperTest` с проверкой корректности разбора всех типов рынков (1X2, тоталы, форы, BTTS, киберспорт, угловые, ЖК).
- **Maven сборка**: компиляция и запуск unit-тестов через `mvn test -pl igaming-source-paf`.
- **K8s Verification**: запуск тестового пода `igaming-source-paf-test` в namespace `igaming-dev`.
- **5-минутный Soak-тест**: непрерывный сбор логов, проверка отсутствия `Exception`, `Error`, `CrashLoopBackOff` и `OOMKilled`.
- **Line Ingestion Check**: проверка наполнения линии от 500 активных матчей в базе данных (`SELECT count(*) FROM match_cache >= 500`).
