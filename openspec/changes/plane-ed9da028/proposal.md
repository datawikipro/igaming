# Proposal: [apuestatotal] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `ed9da028-45fb-40ae-a810-0670a3214a16`
Bookmaker: Apuesta Total (`igaming-source-apuestatotal`, Digitain B2B sports engine platform, Peru & LATAM flagship)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-apuestatotal`:
1. Подготовка и структурирование модуля `igaming-source-apuestatotal` на базе платформы Digitain с полной поддержкой рынка Перу и Латинской Америки (регион `PE` / `LATAM`, домен `apuestatotal.com`).
2. Переход к расширяемой компонентной архитектуре обработчиков рынков (`ApuestatotalMarketHandler`) с поддержкой контекста вида спорта (`SportType`).
3. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core` с поддержкой канонических методов резолвинга ставок (`map`, `supports`).
4. Расширение росписи исходов (с поддержкой локализаций ES/EN/RU):
   - Основные исходы (1X2, Moneyline, таймы/периоды) в `ApuestatotalMatchResultHandler`;
   - Двойной шанс (1X, 12, X2 матча и 1-го тайма) в `ApuestatotalDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No матча и 1-го тайма) в `ApuestatotalBttsHandler`;
   - Тоталы (Over/Under матча, таймов и команд) в `ApuestatotalTotalHandler`;
   - Форы (Handicap / Asian Handicap матча и таймов) в `ApuestatotalHandicapHandler`.
5. Поддержка статистических рынков:
   - `AbstractApuestatotalStatsHandler` — базовый класс для статистических рынков;
   - `ApuestatotalStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `ApuestatotalStatsCardsHandler` — желтые карточки и предупреждения (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
6. Поддержка киберспорта (`ApuestatotalEsportsHandler`, CS2, Dota2, LoL, Valorant и др.):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам на картах (`StatType.ROUNDS`).
7. Разработка всесторонних модульных тестов в `ApuestatotalOddsMapperTest` и тестах хэндлеров, верификация сборки Maven.
