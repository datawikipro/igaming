# Proposal: [esportesdasorte] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `76f7b5a1-a545-4ae4-a686-4bb542e9266a`
Bookmaker: Esportes da Sorte (`igaming-source-esportesdasorte`, Digitain B2B sports engine platform, Brazilian flagship)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-esportesdasorte`:
1. Подготовка и структурирование модуля `igaming-source-esportesdasorte` на базе платформы Digitain с полной поддержкой бразильского рынка (регион `BR` / `LATAM`, домен `esportesdasorte.com`).
2. Переход к расширяемой компонентной архитектуре обработчиков рынков (`EsportesdasorteMarketHandler`) с поддержкой контекста вида спорта (`SportType`).
3. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core` с поддержкой канонических методов резолвинга ставок (`map`, `supports`).
4. Расширение росписи исходов:
   - Основные исходы (1X2, Moneyline, таймы/периоды) в `EsportesdasorteMatchResultHandler`;
   - Двойной шанс (1X, 12, X2 матча и 1-го тайма) в `EsportesdasorteDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No матча и 1-го тайма) в `EsportesdasorteBttsHandler`;
   - Тоталы (Over/Under матча, таймов и команд) в `EsportesdasorteTotalHandler`;
   - Форы (Handicap / Asian Handicap матча и таймов) в `EsportesdasorteHandicapHandler`.
5. Поддержка статистических рынков:
   - `AbstractEsportesdasorteStatsHandler` — базовый класс для статистических рынков;
   - `EsportesdasorteStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `EsportesdasorteStatsCardsHandler` — желтые карточки и предупреждения (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
6. Поддержка киберспорта (`EsportesdasorteEsportsHandler`, CS2, Dota2, LoL, Valorant и др.):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам на картах (`StatType.ROUNDS`).
7. Разработка всесторонних модульных тестов в `EsportesdasorteOddsMapperTest` и тестах хэндлеров, верификация сборки Maven.
