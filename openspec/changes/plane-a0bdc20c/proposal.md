# Proposal: #18: [digitain] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `a0bdc20c-dcf9-4c64-b155-f660353a9654`
Bookmaker: Digitain (`igaming-source-digitain`, Melbet feed)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-digitain`:
1. Переход от монолитного процедурного маппинга в `DigitainOddsMapper` к компонентной ООП-архитектуре обработчиков рынков (`DigitainMarketHandler`).
2. Наследование базовых абстракций `AbstractBetTypeMapper` из `igaming-source-core`.
3. Поддержка углубленной росписи исходов:
   - Основные исходы (1X2, Moneyline, 2-Way, 3-Way, таймы/периоды);
   - Двойной шанс (1X, 12, X2, 1-й тайм);
   - Тоталы (Over/Under матча, индивидуальные тоталы команд, тоталы таймов);
   - Форы (Handicap 1/2, азиатские и европейские форы, форы таймов);
   - Обе забьют (BTTS Yes/No, BTTS в таймах).
4. Поддержка статистических рынков:
   - Угловые (`StatType.CORNERS`: 1X2, тоталы, форы);
   - Желтые карточки (`StatType.YELLOW_CARDS`: 1X2, тоталы, форы).
5. Поддержка киберспорта (`SportType.CYBERSPORT`):
   - Победитель матча (2-Way);
   - Победитель по картам (`BetScope.MAP_1`, `BetScope.MAP_2`, `BetScope.MAP_3`);
   - Фора по картам (`StatType.MAPS`);
   - Тотал карт (`StatType.MAPS`);
   - Тотал раундов (`StatType.ROUNDS`).
6. Расширение запрашиваемых типов ставок в `DigitainMatchService` и WebSocket feed для получения полной росписи.
7. Покрытие unit-тестами всей иерархии мапперов и верификация сборки.
