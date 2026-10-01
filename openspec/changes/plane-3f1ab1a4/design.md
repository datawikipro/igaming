# Architecture Design: [paf] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-paf` осуществляет сбор линий и котировок финского лицензированного букмекера PAF (Åland Gaming / Paf.com), работающего на платформе Kambi API (`https://eu.offering-api.kambicdn.com/offering/v2018`).
Цель рефакторинга — создание модульной ООП-архитектуры обработчиков рынков (`PafMarketHandler`) с поддержкой расширенной росписи исходов, статистических маркетов (угловые, ЖК) и киберспортивных дисциплин (CS2, Dota 2, League of Legends, Valorant и др.) с учетом локализаций (английский, финский, шведский языки).

## Component Structure

```
pro.datawiki.igaming.source.paf
├── PafApplication (Spring Boot main class с @EntityScan и @EnableJpaRepositories)
├── config
│   └── PafConfig (настройки API, прокси 100.83.113.50:3128, RestTemplate bean)
├── scheduler
│   └── MatchFetchScheduler (периодический запуск discovery и scrape)
└── service
    ├── PafApiClient (HTTP-клиент Kambi API listView & betoffer)
    ├── PafDiscoveryService (обнаружение событий по видам спорта, дедупликация)
    ├── MatchService (оркестрация сбора, маппинг, push в aggregator)
    ├── PafOddsMapper (extends AbstractBetTypeMapper, перегрузка map с SportType, цепочка хэндлеров)
    └── handler
        ├── PafMarketHandler (интерфейс обработчика: supports, handleOffer, handle)
        ├── AbstractPafMarketHandler (базовый класс с хелперами resolveScope, extractDecimalOdds, addOddItem, addMatchResult, addTotal, addHandicap, addBinary)
        ├── AbstractPafStatsHandler (базовый класс для статистики: тоталы, форы, 1X2, двойной шанс)
        ├── PafMoneylineHandler (1X2, Moneyline 2-way/3-way, исходы таймов/периодов/четвертей/сетов)
        ├── PafTotalHandler (общие и индивидуальные тоталы Over/Under матча и таймов)
        ├── PafHandicapHandler (европейские и азиатские форы матча и таймов)
        ├── PafDoubleChanceHandler (Двойной шанс: 1X, 12, X2 матча и 1-го тайма, мультиязычный резолвинг)
        ├── PafBttsHandler (Обе забьют: Yes/No матча и 1-го тайма)
        ├── PafDrawNoBetHandler (Ничья нет ставки / Фора 0.0 матча и 1-го тайма)
        ├── PafStatsCornersHandler (Угловые: 1X2, тоталы, форы с StatType.CORNERS)
        ├── PafStatsCardsHandler (ЖК/карточки: 1X2, тоталы, форы с StatType.YELLOW_CARDS)
        └── PafEsportsHandler (Киберспорт: победители карт BetScope.MAP_1..5, тотал/фора карт StatType.MAPS, раунды StatType.ROUNDS)
```

## Market Handlers & Order Hierarchy
- **`PafEsportsHandler` (`@Order(5)`)**: перехватывает киберспортивные дисциплины (`SportType.CS2`, `DOTA2`, `LEAGUE_OF_LEGENDS`, `VALORANT`, `ESPORTS` и др.), маппит карты (`BetScope.MAP_1..5`), тоталы карт (`StatType.MAPS`) и раундов (`StatType.ROUNDS`).
- **`PafStatsCornersHandler` (`@Order(7)`)**: перехватывает угловые (ключевые слова "corner", "hörn", "kulma", "kulmapotku") с `StatType.CORNERS`.
- **`PafStatsCardsHandler` (`@Order(8)`)**: перехватывает желтые карточки ("card", "booking", "kort", "varning", "kortti", "varoitus") с `StatType.YELLOW_CARDS`.
- **`PafTotalHandler` (`@Order(20)`)**: тоталы голов/очков Over/Under, исключая статистику и киберспортивные раунды.
- **`PafDoubleChanceHandler` (`@Order(25)`)**: двойной шанс с поддержкой типов Kambi (`OT_ONE_CROSS`, `OT_ONE_TWO`, `OT_CROSS_TWO`) и текстовых меток (1X, 12, X2, Home/Draw, Hemma/Oavgjort, Tuplamahdollisuus).
- **`PafBttsHandler` (`@Order(30)`)**: обе забьют Yes/No (`OT_YES`, `OT_NO`, Ja/Nej, Kyllä/Ei).
- **`PafHandicapHandler` (`@Order(30)`)**: форы матча и периодов.
- **`PafDrawNoBetHandler` (`@Order(35)`)**: DNB матча и таймов -> `HandicapBet` с форой 0.0.
- **`PafMoneylineHandler` (`@Order(50)`)**: основные исходы 1X2 / 2-way Moneyline.

## Multilingual Support
Kambi API для PAF предоставляет метрики на английском (`englishLabel`), а также на локальных языках финском/шведском (`label`).
Обработчики нормализуют и поддерживают:
- Английский (English): "Corners", "Yellow Cards", "Double Chance", "Both Teams To Score", "Draw No Bet", "Map 1 Winner", "Total Rounds".
- Финский (Finnish): "Kulmapotkut", "Kortit / Varoitukset", "Tuplamahdollisuus", "Molemmat joukkueet tekevät maalin (Kyllä/Ei)", "Tasapeli ei vetoa", "Kartan 1 voittaja", "Kierrokset yhteensä".
- Шведский (Swedish): "Hörnor", "Kort / Varningar", "Dubbelchans", "Båda lagen gör mål (Ja/Nej)", "Oavgjort inget spel".

## Testing Strategy
Модульное тестирование `PafOddsMapperTest`:
1. **Основные рынки** (1X2, Moneyline 2-way/3-way, таймы, четверти, периоды, сеты, тоталы, форы).
2. **Роспись исходов** (Double Chance, BTTS, DNB с проверкой различных локализаций и форматов).
3. **Статистика** (Corners, Cards/Bookings — тоталы, форы, 1X2, таймы).
4. **Киберспорт** (CS2, Dota2, LoL, Valorant — победители карт 1-5, тотал/фора карт, тотал/фора раундов).
5. **Граничные случаи** (пустые события/офферы, невалидные коэффициенты, fallback resolver, unmapped bet persistence).
