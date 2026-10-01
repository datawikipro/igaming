# Architecture Design: [betnacional] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## 1. Архитектурный паттерн Strategy / Market Handler
Модуль `igaming-source-betnacional` декомпозирован на независимые обработчики рынков:
- `BetnacionalMarketHandler`: контракт обработчика с методами `boolean supports(BetnacionalMarketDto market, SportType sportType)` и `void handle(BetnacionalMarketDto market, BetnacionalEventDto event, SportType sportType, List<OddItem> items)`.
- `AbstractBetnacionalMarketHandler`: базовый класс, реализующий извлечение чисел, определение `BetScope` (таймы, периоды, карты), проверку киберспортивных дисциплин и безопасное добавление исходов `addOddItem(...)`.

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
- `BetnacionalOddsMapper`: агрегирует список всех зарегистрированных `BetnacionalMarketHandler`, трансформирует `BetnacionalEventDto` в `OddsUpdateRequest`.
- `BetnacionalApiClient`: HTTP-клиент к Betnacional API с поддержкой корректных заголовков и прокси.
- `BetnacionalMatchService`: сервис оркестрации парсинга, сохранения снапшотов в `match_cache` и отправки обновлений в Aggregator API.
- `BetnacionalScheduler`: фоновый планировщик сбора линий.
