# Architecture Design: [wplay] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## 1. Архитектурный паттерн Strategy / Market Handler
Модуль `igaming-source-wplay` переводится с монолитного regex-парсинга на модульную ООП-архитектуру на базе паттерна Strategy:
- `WplayMarketHandler`: контракт обработчика с методами:
  - `boolean supports(WplayMarketDto market, SportType sportType)`
  - `void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items)`
- `AbstractWplayMarketHandler`: базовый абстрактный класс, расширяющий `AbstractBetTypeMapper` и реализующий общую утилитную логику:
  - `addOddItem(...)`: валидация и безопасное добавление исхода с генерацией уникального `factorId`.
  - `extractNumber(...)`: извлечение числовых параметров (тоталы, форы) из названий рынков и исходов.
  - `resolveScope(...)`: распознавание скоупа события (`FULL_MATCH`, `HALF_1`, `HALF_2`, `MAP_1`..`MAP_7`, `ROUND_1`..`ROUND_5`, `PERIOD_1`..`PERIOD_3`).
  - `isEsports(...)`: детекция киберспортивных дисциплин (CS2, DOTA2, LoL, VALORANT, ESPORTS и др.).
  - `isTeam1(...)` / `isTeam2(...)`: сопоставление исхода с командами/участниками.

## 2. Специализированные обработчики рынков
В пакете `pro.datawiki.igaming.source.wplay.service.handler`:
1. `MatchResultMarketHandler`: 1X2, Moneyline, W1/X/W2 для матча и периодов.
2. `DoubleChanceMarketHandler`: 1X, 12, X2 (Double Chance / Doble Oportunidad).
3. `TotalMarketHandler`: Over/Under тоталы (общие и индивидуальные команд).
4. `HandicapMarketHandler`: форы (европейские и азиатские спрэды).
5. `BothTeamsToScoreMarketHandler`: BTTS Yes/No (Ambos Equipos Anotan).
6. `DrawNoBetMarketHandler`: DNB рынки, нормализуемые в форы 0.0 (`HandicapBet`).
7. `CorrectScoreMarketHandler`: точный счет (Marcador Exacto).
8. `HalfTimeFullTimeMarketHandler`: тайм/матч (Descanso/Final).
9. `PeriodMarketHandler`: исходы 1-го и 2-го таймов, четвертей, периодов.
10. `CornersMarketHandler`: тоталы и форы угловых ударов (`StatType.CORNERS`, Tiros de Esquina).
11. `CardsMarketHandler`: тоталы, форы и карточки (`StatType.YELLOW_CARDS`, Tarjetas / Amarillas).
12. `EsportsMarketHandler`: исходы киберспортивных матчей, победители карт, тоталы карт и раундов, убийства, First Blood.

## 3. Модель данных (DTO)
В пакете `pro.datawiki.igaming.source.wplay.dto`:
- `WplayEventDto`: представление события с полями `id`, `name`, `team1`, `team2`, `sportName`, `leagueName`, `isLive`, `startTime`, `markets`.
- `WplayMarketDto`: представление маркета с полями `id`, `name`, `marketType`, `outcomes`.
- `WplayOutcomeDto`: представление исхода с полями `id`, `name`, `odds`, `price`, `param`.
- `WplayResponseDto`: контейнер ответа со списком событий.

## 4. Оркестрация и обратная совместимость
- `WplayOddsMapper`: хранит коллекцию зарегистрированных обработчиков `List<WplayMarketHandler>`.
- Предоставляет метод `mapEventToOddsUpdateRequest(WplayEventDto event)` для обработки DTO.
- Сохраняет и расширяет метод `mapHtmlToOddsUpdateRequest(MatchCache cache, String html)`: производит парсинг HTML-структуры Playtech в DTO-модели и делегирует обработку соответствующим handler-классам.
