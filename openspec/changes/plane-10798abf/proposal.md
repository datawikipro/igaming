# Proposal: [atg] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `10798abf-6084-4ccd-8812-4ebec0535ad3`
Bookmaker: ATG (`igaming-source-atg`)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-atg` (шведский оператор ATG на платформе Kambi):
1. Переход к расширяемой компонентной архитектуре обработчиков рынков (`AtgMarketHandler`) с поддержкой контекста вида спорта (`SportType`).
2. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core` с поддержкой канонических методов резолвинга ставок (`map`, `supports`).
3. Расширение росписи исходов:
   - Основные исходы (1X2, Moneyline, таймы/периоды) в `AtgMoneylineHandler`;
   - Двойной шанс (1X, 12, X2 матча и 1-го тайма) в `AtgDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No матча и 1-го тайма) в `AtgBttsHandler`;
   - Ничья нет ставки (DNB матча и 1-го тайма) в `AtgDrawNoBetHandler`;
   - Тоталы (Over/Under матча, таймов и команд) в `AtgTotalHandler`;
   - Форы (Handicap / Asian Handicap матча и таймов) в `AtgHandicapHandler`.
4. Поддержка статистических рынков:
   - `AbstractAtgStatsHandler` — базовый класс для статистических рынков;
   - `AtgStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `AtgStatsCardsHandler` — желтые карточки и предупреждения (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
5. Поддержка киберспорта (`AtgEsportsHandler`, CS2, Dota2, LoL, Valorant и др.):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам на картах (`StatType.ROUNDS`).
6. Разработка всесторонних модульных тестов в `AtgOddsMapperTest` и валидация сборки Maven.
