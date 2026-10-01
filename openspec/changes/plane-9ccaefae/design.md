# Design: [betano] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration

Модуль `igaming-source-betano` спроектирован как высокопроизводительный микросервис сбора данных, основанный на Spring Boot 3.4 (Java 21) и архитектурном фреймворке `igaming-source-core`.

### Компоненты системы:
1. **`BetanoApiClient`**:
   - HTTP-клиент на базе Apache HttpClient 5 и `RestTemplate`.
   - Подключение через выделенный HTTP-прокси (`http://100.83.113.50:3128` / `proxy-us.service-proxy.svc.cluster.local:31292`).
   - Запросы к Kambi Offering API:
     - `GET /offering/v2018/kambi/listView/{sportSlug}.json?lang={locale}&market={market}` — получение активных событий по спорту.
     - `GET /offering/v2018/kambi/betoffer/event/{eventId}.json?lang={locale}&market={market}` — детальная роспись исходов события.
   - Заголовки: эмуляция реального браузера с Origin `https://www.betano.com`.

2. **`BetanoOddsMapper`**:
   - Наследует `AbstractKambiOddsMapper` из `igaming-source-core`.
   - Имя букмекера: `betano`.
   - Регионы: `GLOBAL`, `EU`.
   - Маппинг рынков через ООП-стратегии:
     - `MatchResultBet` (П1, X, П2, Moneyline)
     - `TotalBet` (Over/Under тоталы матча и индивидуальные тоталы команд)
     - `HandicapBet` (Европейские и азиатские форы)
     - `BinaryMarketBet` (Обе забьют / BTTS)
     - `DoubleChanceBet` (1X, 12, X2)
     - `DrawNoBet`
     - Статистические маркеты (Угловые и желтые карточки)
     - Киберспортивные маркеты (Карта 1/2, победитель матча в CS2, Dota 2, LoL, Valorant).

3. **`BetanoDiscoveryService`**:
   - Сканирование 16 видов спорта (`football`, `tennis`, `basketball`, `ice_hockey`, `baseball`, `esports`, `volleyball`, `handball`, `table_tennis`, `cricket`, `golf`, `boxing`, `motorsports`, `american_football`, `darts`, `snooker`).
   - Дедупликация и сохранение в локальный кэш `match_cache` через `MatchPersistenceService`.
   - Защита от переполнения кэша (LRU/очистка при достижении порога).

4. **`MatchService` & `MatchFetchScheduler`**:
   - Наследование от `AbstractBaseBookmakerService` (`getBookmakerFamily() = "betano"`).
   - Формирование `OddsUpdateRequest` и отправка котировок в `AggregatorClient` (`http://igaming-aggregator.igaming-dev.svc.cluster.local:8080`).
   - Периодический опрос линий и отправка Heartbeat каждые 60 секунд.

5. **K8s & Database Architecture**:
   - Сборка OCI-образа через Jib: `100.78.183.101:30500/igaming-source-betano:latest`.
   - Неблокирующий HikariCP (`initialization-fail-timeout=0`, `connection-timeout=5000`).
   - Health probes Actuator: `/actuator/health/liveness`, `/actuator/health/readiness`.
   - PostgreSQL 16 StatefulSet в namespace `igaming-source` (`igaming-source-betano-db`) с параметрами `synchronous_commit=off`, `fsync=off` в `emptyDir` tmpfs.

## Verification & Deployment Strategy
- **Unit-тестирование**: `BetanoOddsMapperTest` с проверкой корректности разбора всех типов рынков (1X2, тоталы, форы, BTTS, киберспорт, угловые).
- **Maven сборка**: компиляция и упаковка OCI-образа через `mvn -pl igaming-source-betano compile jib:build`.
- **K8s Verification**: запуск тестового пода `igaming-source-betano-test` в namespace `igaming-dev`.
- **5-минутный Soak-тест**: непрерывный сбор логов, проверка отсутствия `Exception`, `Error`, `CrashLoopBackOff` и `OOMKilled`.
- **Line Ingestion Check**: проверка наполнения линии от 500 активных матчей в базе данных (`SELECT count(*) FROM match_cache >= 500`).
