# Proposal: [sbobet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `c6868efa-ef3e-4b70-8024-d59dfe47c2e3`
Bookmaker: SBOBET (`igaming-source-sbobet`)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-sbobet`:
1. Переход к расширяемой компонентной архитектуре обработчиков рынков (`SbobetMarketHandler`) с поддержкой контекста вида спорта (`SportType`).
2. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core` с поддержкой канонических методов резолвинга ставок.
3. Расширение росписи исходов:
   - Основные исходы (1X2, Moneyline, таймы) в `SbobetMoneylineHandler`;
   - Двойной шанс (1X, 12, X2 матча и 1-го тайма) в `SbobetDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No матча и 1-го тайма) в `SbobetBttsHandler`;
   - Ничья нет ставки (DNB матча и 1-го тайма) в `SbobetDrawNoBetHandler`;
   - Тоталы (Over/Under матча и таймов) в `SbobetTotalHandler`;
   - Форы (Handicap матча и таймов) в `SbobetHandicapHandler`.
4. Поддержка статистических рынков:
   - `AbstractSbobetStatsHandler` — базовый класс для статистических рынков;
   - `SbobetStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `SbobetStatsCardsHandler` — желтые карточки (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
5. Поддержка киберспорта (`SbobetEsportsHandler`, CS2, Dota2, LoL, Valorant и др.):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам на картах (`StatType.ROUNDS`).
6. Разработка всесторонних модульных тестов и валидация сборки.
