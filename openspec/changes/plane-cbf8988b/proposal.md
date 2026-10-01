# Proposal: [bwin] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `cbf8988b-ab42-4f5f-b80e-2eaa022c0671`
Bookmaker: Bwin (`igaming-source-bwin`, Entain platform / CDS API)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-bwin`:
1. Подготовка и структурирование модуля `igaming-source-bwin` на базе платформы Entain (CDS API) с поддержкой международного букмекера Bwin.
2. Переход к расширяемой компонентной архитектуре обработчиков рынков (`BwinMarketHandler`) на базе паттерна Strategy / Handler с поддержкой контекста вида спорта (`SportType`).
3. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core` с поддержкой канонических методов резолвинга ставок (`map`, `supports`).
4. Расширение росписи исходов:
   - Основные исходы (1X2, Moneyline, таймы/периоды) в `BwinMatchResultHandler`;
   - Двойной шанс (1X, 12, X2 матча и таймов) в `BwinDoubleChanceHandler`;
   - Ничья исключена (Draw No Bet -> фора 0.0) в `BwinDrawNoBetHandler`;
   - Обе забьют (BTTS Yes/No матча и таймов) в `BwinBttsHandler`;
   - Тоталы (Over/Under матча, таймов и команд) в `BwinTotalHandler`;
   - Форы (Handicap / Asian Handicap матча и таймов) в `BwinHandicapHandler`;
   - Точный счет (Correct Score матча и таймов) в `BwinCorrectScoreHandler`.
5. Поддержка статистических рынков:
   - `AbstractBwinStatsHandler` — базовый класс для статистических рынков;
   - `BwinStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `BwinStatsCardsHandler` — желтые карточки и предупреждения (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
6. Поддержка киберспорта (`BwinEsportsHandler`, CS2, Dota 2, LoL, Valorant и др.):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам (`StatType.ROUNDS`);
   - Специфичные исходы (First Blood).
7. Интеграция цепочки обработчиков в `BwinOddsMapper` с приоритезацией `@Order` и безопасным фоллбэком на `BetTypeResolverService`.
8. Разработка всесторонних модульных тестов в `BwinOddsMapperTest` и верификация сборки Maven.
