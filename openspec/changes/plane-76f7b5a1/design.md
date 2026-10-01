# Architecture Design: [esportesdasorte] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-esportesdasorte` осуществляет сбор линий и котировок бразильского букмекера Esportes da Sorte, функционирующего на базе платформы Digitain B2B sports engine.
Цель рефакторинга — внедрение компонентной ООП-архитектуры обработчиков рынков (`EsportesdasorteMarketHandler`) на основе `AbstractBetTypeMapper` и паттерна Chain of Responsibility / Strategy с поддержкой контекста вида спорта (`SportType`), расширенной росписи исходов, статистических маркетов (угловые, ЖК) и киберспорта (CS2, Dota 2, League of Legends и др.).

## Component Structure

```
pro.datawiki.igaming.source.esportesdasorte.service
├── EsportesdasorteOddsMapper (extends AbstractBetTypeMapper, iterates marketHandlers with SportType)
└── handler
    ├── EsportesdasorteMarketHandler (интерфейс обработчика маркета)
    ├── AbstractEsportesdasorteMarketHandler (базовый абстрактный класс с хелперами resolveScope, createOddItem, addOddItem)
    ├── EsportesdasorteMatchResultHandler (1X2, Moneyline, исходы таймов/периодов)
    ├── EsportesdasorteDoubleChanceHandler (Двойной шанс матча и таймов: 1X, 12, X2)
    ├── EsportesdasorteTotalHandler (Тоталы матча, индивидуальные тоталы, тоталы таймов)
    ├── EsportesdasorteHandicapHandler (Форы матча и таймов, European & Asian Handicap)
    ├── EsportesdasorteBttsHandler (Обе забьют матча и таймов: Yes / No)
    ├── AbstractEsportesdasorteStatsHandler (базовый класс для статистических маркетов)
    ├── EsportesdasorteStatsCornersHandler (Угловые: исходы 1X2, тоталы, форы с StatType.CORNERS)
    ├── EsportesdasorteStatsCardsHandler (ЖК: исходы 1X2, тоталы, форы с StatType.YELLOW_CARDS)
    └── EsportesdasorteEsportsHandler (Киберспорт: победители карт MAP_1..5, тотал/фора карт StatType.MAPS, раунды StatType.ROUNDS)
```

## Stake Types & Feed Protocol
Взаимодействие с платформой Digitain осуществляется через WebSocket/REST API. Обрабатываемые типы ставок:
- 1, 702: Match Winner / 1X2
- 2: Handicap
- 3: Total
- 4, 7: Half 1 / Half 2 1X2
- 5, 8: Half 1 / Half 2 Handicap
- 6, 9: Half 1 / Half 2 Total
- 10, 46: Both Teams To Score
- 166, 167, 168: Corners (Total, Handicap, 1X2)
- 188, 189: Yellow Cards (Total, Handicap, 1X2)
- 703, 704, 705: Map 1/2/3 Winner
- 740, 741: Map Handicap
- 742: Map Total
- 992, 993: Double Chance (Match, 1st Half)

## Testing Strategy
- Разработка unit-тестов в `pro.datawiki.igaming.source.esportesdasorte.service.EsportesdasorteOddsMapperTest` и пакете `handler`:
  1. Основные рынки: 1X2, Moneyline, таймы/периоды, тоталы, форы;
  2. Роспись исходов: двойной шанс, обе забьют;
  3. Статистические рынки: угловые (1X2, тотал, фора), желтые карточки;
  4. Киберспортивные дисциплины: победа по картам (MAP_1..5), фора карт, тотал карт, тотал раундов;
  5. Граничные случаи: пустые и некорректные группы, null-значения, коэффициенты <= 1.0.
