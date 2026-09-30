# Architecture Design: #23: [betway] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## 1. Архитектурный паттерн Strategy / Market Handler
Модуль `igaming-source-betway` декомпозирован на независимые обработчики рынков:
- `BetwayMarketHandler`: контракт обработчика с методами `boolean supports(BetwayMarketDto market, SportType sportType)` и `void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items)`.
- `AbstractBetwayMarketHandler`: базовый класс, реализующий извлечение чисел, определение `BetScope` (таймы, периоды, карты), проверку киберспортивных дисциплин и безопасное добавление исходов `addOddItem(...)`.

## 2. Специализированные обработчики рынков
- `MatchResultMarketHandler`: 1X2, Moneyline, W1/X/W2 для основного времени и периодов.
- `DoubleChanceMarketHandler`: 1X, 12, X2 с распознаванием текстовых названий команд.
- `TotalMarketHandler`: Over/Under тоталы матча и индивидуальные тоталы команд (`BetSubject.TEAM1`, `BetSubject.TEAM2`).
- `HandicapMarketHandler`: азиатские и европейские форы / спрэды.
- `DrawNoBetMarketHandler`: DNB рынки, транслируемые в форы 0.0.
- `BothTeamsToScoreMarketHandler`: BTTS Yes/No (`BinaryMarketBet`).
- `CorrectScoreMarketHandler`: точный счет матча (`CorrectScoreBet`).
- `HalfTimeFullTimeMarketHandler`: тайм/матч исходы (`HalfTimeFullTimeBet`).
- `PeriodMarketHandler`: исходы отдельных периодов и четвертей.
- `CornersMarketHandler`: тоталы и форы угловых ударов (`StatType.CORNERS`).
- `CardsMarketHandler`: тоталы и форы желтых карточек (`StatType.YELLOW_CARDS`).
- `EsportsMarketHandler`: исходы матча, карт (Map 1..5), тоталы раундов/карт, форы карт, First Blood.

## 3. Оркестрация и маппинг
- `BetwayOddsMapper`: агрегирует список всех зарегистрированных `BetwayMarketHandler`, трансформирует `BetwayEventDto` в `OddsUpdateRequest`.
- `BetwayApiClient`: HTTP-клиент к Betway Events API с поддержкой корректных заголовков.
- `BetwayMatchService`: сервис оркестрации парсинга, сохранения снапшотов в `match_cache` и отправки обновлений в Kafka/Aggregator API.
- `BetwayScheduler`: фоновый планировщик сбора линий.
