# Architecture Design: [bet7k] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## 1. Архитектурный паттерн Strategy / Market Handler
Модуль `igaming-source-bet7k` декомпозирован на независимые обработчики рынков:
- `Bet7kMarketHandler`: контракт обработчика с методами `boolean supports(Bet7kMarketDto market, SportType sportType)` и `void handle(Bet7kMarketDto market, Bet7kEventDto event, SportType sportType, List<OddItem> items)`.
- `AbstractBet7kMarketHandler`: базовый класс, реализующий извлечение чисел, определение `BetScope` (таймы, периоды, карты), проверку киберспортивных дисциплин и безопасное добавление исходов `addOddItem(...)`.

## 2. Специализированные обработчики рынков
- `MatchResultMarketHandler`: 1X2, Moneyline, W1/X/W2 для основного времени и периодов (с поддержкой португальской локализации Resultado Final, Vencedor).
- `DoubleChanceMarketHandler`: 1X, 12, X2 (Dupla Chance, Chance Dupla).
- `TotalMarketHandler`: Over/Under тоталы матча и индивидуальные тоталы команд (`BetSubject.TEAM1`, `BetSubject.TEAM2`, Mais/Menos, Acima/Abaixo).
- `HandicapMarketHandler`: азиатские и европейские форы / спрэды (Handicap Asiatico, Desvantagem).
- `DrawNoBetMarketHandler`: DNB рынки (Empate Anula Aposta), транслируемые в форы 0.0.
- `BothTeamsToScoreMarketHandler`: BTTS Yes/No (Ambas Marcam: Sim/Não, `BinaryMarketBet`).
- `CorrectScoreMarketHandler`: точный счет матча (Resultado Exato, `CorrectScoreBet`).
- `HalfTimeFullTimeMarketHandler`: тайм/матч исходы (Intervalo / Final, `HalfTimeFullTimeBet`).
- `PeriodMarketHandler`: исходы отдельных периодов, таймов и четвертей.
- `CornersMarketHandler`: тоталы и форы угловых ударов (Escanteios, `StatType.CORNERS`).
- `CardsMarketHandler`: тоталы и форы желтых карточек (Cartões Amarelos, `StatType.YELLOW_CARDS`).
- `EsportsMarketHandler`: исходы матча, карт (Map 1..5), тоталы раундов/карт, форы карт, First Blood.

## 3. Оркестрация и маппинг
- `Bet7kOddsMapper`: агрегирует список всех зарегистрированных `Bet7kMarketHandler`, трансформирует `Bet7kEventDto` в `OddsUpdateRequest`.
- `Bet7kApiClient`: HTTP-клиент к Bet7k API с поддержкой корректных заголовков и прокси.
- `Bet7kMatchService`: сервис оркестрации парсинга, сохранения снапшотов в `match_cache` и отправки обновлений в Aggregator API.
- `Bet7kScheduler`: фоновый планировщик сбора линий.
