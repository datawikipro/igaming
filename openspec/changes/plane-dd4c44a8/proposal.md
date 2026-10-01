# Proposal: [vaidebet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `dd4c44a8-c49e-468b-bafa-55e9913a8fd8`
Bookmaker: Vai de Bet (`igaming-source-vaidebet`, Digitain B2B sports engine platform, Brazilian flagship)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-vaidebet`:
1. Подготовка и структурирование модуля `igaming-source-vaidebet` на базе платформы Digitain с полной поддержкой бразильского рынка (регион `BR` / `LATAM`, домен `vaidebet.com`).
2. Переход к расширяемой компонентной архитектуре обработчиков рынков (`VaidebetMarketHandler`) с поддержкой контекста вида спорта (`SportType`).
3. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core` с поддержкой канонических методов резолвинга ставок (`map`, `supports`).
4. Расширение росписи исходов:
   - Основные исходы (1X2, Moneyline, таймы/периоды) в `VaidebetMatchResultHandler`;
   - Двойной шанс (1X, 12, X2 матча и 1-го тайма) в `VaidebetDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No матча и 1-го тайма) в `VaidebetBttsHandler`;
   - Тоталы (Over/Under матча, таймов и команд) в `VaidebetTotalHandler`;
   - Форы (Handicap / Asian Handicap матча и таймов) в `VaidebetHandicapHandler`.
5. Поддержка статистических рынков:
   - `AbstractVaidebetStatsHandler` — базовый класс для статистических рынков;
   - `VaidebetStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `VaidebetStatsCardsHandler` — желтые карточки и предупреждения (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
6. Поддержка киберспорта (`VaidebetEsportsHandler`, CS2, Dota2, LoL, Valorant и др.):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам на картах (`StatType.ROUNDS`).
7. Разработка всесторонних модульных тестов в `VaidebetOddsMapperTest` и тестах хэндлеров, верификация сборки Maven.
