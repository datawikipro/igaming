# Architecture Design: [10bet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## 1. Архитектурный паттерн Strategy / Market Handler
Модуль `igaming-source-10bet` декомпозирован на независимые обработчики рынков:
- `TenBetMarketHandler`: контракт обработчика с методами `boolean supports(TenBetMarketDto market, SportType sportType)` и `void handle(TenBetMarketDto market, TenBetEventDto event, SportType sportType, List<OddItem> items)`.
- `AbstractTenBetMarketHandler`: базовый класс, реализующий извлечение чисел, определение `BetScope` (таймы, периоды, карты), проверку киберспортивных дисциплин и безопасное добавление исходов `addOddItem(...)`.

## 2. Специализированные обработчики рынков
- `MatchResultMarketHandler`: 1X2, Moneyline, W1/X/W2 для основного времени и периодов.
- `DoubleChanceMarketHandler`: 1X, 12, X2 (Double Chance).
- `TotalMarketHandler`: Over/Under тоталы матча и индивидуальные тоталы команд (`BetSubject.TEAM1`, `BetSubject.TEAM2`).
- `HandicapMarketHandler`: азиатские и европейские форы / спрэды.
- `DrawNoBetMarketHandler`: DNB рынки, транслируемые в форы 0.0 (`HandicapBet`).
- `BothTeamsToScoreMarketHandler`: BTTS Yes/No (`BinaryMarketBet`).
- `CorrectScoreMarketHandler`: точный счет матча (`CorrectScoreBet`).
- `HalfTimeFullTimeMarketHandler`: тайм/матч исходы (`HalfTimeFullTimeBet`).
- `PeriodMarketHandler`: исходы отдельных периодов, таймов и четвертей.
- `CornersMarketHandler`: тоталы и форы угловых ударов (`StatType.CORNERS`).
- `CardsMarketHandler`: тоталы и форы желтых карточек (`StatType.YELLOW_CARDS`).
- `EsportsMarketHandler`: исходы матча, карт (Map 1..7), тоталы раундов/карт, форы карт, First Blood.

## 3. Оркестрация и маппинг
- `TenBetOddsMapper`: агрегирует список всех зарегистрированных `TenBetMarketHandler`, трансформирует `TenBetEventDto` в `OddsUpdateRequest`.
- `TenBetApiClient`: HTTP-клиент к 10bet API с поддержкой корректных заголовков и прокси.
- `TenBetMatchService`: сервис оркестрации парсинга, сохранения снапшотов в `match_cache` и отправки обновлений в Aggregator API.
- `TenBetScheduler`: фоновый планировщик сбора линий.
