# Design: #40: [smarkets] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration

Модуль `igaming-source-smarkets` спроектирован как высокопроизводительный сервис сбора данных биржи ставок Smarkets (P2P Orderbook), основанный на Spring Boot 3.4 (Java 21) и архитектурном фреймворке `igaming-source-core`.

### Компоненты системы:
1. **`SmarketsMarketHandler`**:
   - Интерфейс обработчика рынка с методами:
     - `boolean supports(SmarketsMarket market, SportType sportType);`
     - `void handle(SmarketsEvent event, SmarketsMarket market, List<SmarketsContract> contracts, Map<String, SmarketsContractQuotes> quotesByContractId, SportType sportType, String team1, String team2, List<OddItem> items);`

2. **`AbstractSmarketsMarketHandler`**:
   - Базовый абстрактный класс, реализующий вспомогательные методы:
     - Извлечение бэк-котировок: расчет decimal odds через `quotes.getBestBackOdds()`.
     - `addOddItem`: безопасное добавление исхода с округлением и валидацией минимального коэффициента (> 1.0).
     - `resolveScope`: резолвинг `BetScope` (FULL_MATCH, HALF_1, HALF_2, MAP_1..MAP_5, PERIOD_1..PERIOD_3, QUARTER_1..QUARTER_4).
     - `extractParam`: извлечение числовой форы/тотала из `marketType.param` или имени маркета.
     - `determineTotalSubject`: определение `BetSubject` (MATCH, TEAM1, TEAM2).
     - Утилиты проверки киберспорта и статистики.

3. **Специализированные обработчики росписи (Strategy / Chain of Responsibility)**:
   - `SmarketsMatchResultHandler` (`@Order(10)`): Full-time result, Winner 3-way, Winner 2-way, Moneyline, исходы таймов/периодов.
   - `SmarketsTotalHandler` (`@Order(20)`): Over/Under тоталы матча, таймов, периодов, индивидуальные тоталы команд.
   - `SmarketsHandicapHandler` (`@Order(30)`): Азиатские и европейские форы / спреды с точным парсингом знака и значений.
   - `SmarketsDoubleChanceHandler` (`@Order(40)`): Двойной шанс 1X, 12, X2 (матч и таймы).
   - `SmarketsDrawNoBetHandler` (`@Order(50)`): Ничья нет ставки (DNB).
   - `SmarketsBttsHandler` (`@Order(60)`): Обе команды забьют (BTTS Yes/No).
   - `SmarketsCorrectScoreHandler` (`@Order(70)`): Точный счет матча.
   - `SmarketsEsportsHandler` (`@Order(15)`): Киберспорт (CS2, Dota 2, LoL, Valorant) — победитель матча/карт, тоталы карт/раундов, форы, First Blood.
   - `SmarketsStatsHandler` (`@Order(25)`): Угловые удары (`StatType.CORNERS`) и карточки (`StatType.YELLOW_CARDS`, `StatType.CARDS`) — 1X2, тоталы, форы.

4. **`SmarketsOddsMapper`**:
   - Наследует `AbstractBetTypeMapper`.
   - Внедрение упорядоченного списка `List<SmarketsMarketHandler>` через Spring dependency injection.
   - Наличие fallback-конструктора со списком обработчиков по умолчанию для прямого инстанцирования в unit-тестах.

5. **Тестирование**:
   - `SmarketsOddsMapperTest`: тесты всех типов маркетов (1X2, тоталы, форы, двойной шанс, DNB, BTTS, точный счет, статистика, киберспорт).
