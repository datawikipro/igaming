# Proposal: #40: [smarkets] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `51b736fd-84c4-4203-a18e-14cbfb294613`
- **Bookmaker**: Smarkets (European Betting Exchange, `smarkets.com`)
- **Target Module**: `igaming-source-smarkets`

## Description
Реализовать модульную архитектуру и ООП-рефакторинг мапперов котировок биржи ставок Smarkets с выделением специализированных обработчиков рынков (паттерн Strategy / Chain of Responsibility):
1. **Базовая инфраструктура обработчиков**: базовый интерфейс `SmarketsMarketHandler`, абстрактный класс `AbstractSmarketsMarketHandler` с утилитными методами (скоупы, субъекты, парсинг параметров, расчет бэк-кэфов).
2. **Основная роспись**: `SmarketsMatchResultHandler` (1X2, Winner, Moneyline), `SmarketsTotalHandler` (Over/Under тоталы матча и таймов), `SmarketsHandicapHandler` (азиатские и европейские форы), `SmarketsDoubleChanceHandler` (двойной шанс), `SmarketsDrawNoBetHandler` (ничья нет ставки), `SmarketsBttsHandler` (обе забьют), `SmarketsCorrectScoreHandler` (точный счет).
3. **Киберспорт**: `SmarketsEsportsHandler` (CS2, Dota 2, League of Legends, Valorant: победа на карте, тоталы карт/раундов, форы, First Blood).
4. **Статистика**: `SmarketsStatsHandler` (угловые удары `StatType.CORNERS`, желтые карточки `StatType.YELLOW_CARDS`).
5. **Маппер**: `SmarketsOddsMapper` с инжекцией упорядоченной цепочки обработчиков `List<SmarketsMarketHandler>` и дефолтным фоллбэком.
6. **Тестирование**: всесторонний набор модульных тестов в `SmarketsOddsMapperTest`.
