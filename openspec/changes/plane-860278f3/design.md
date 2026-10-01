# Architecture Design: [wplay] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Overview & Goals
Модуль `igaming-source-wplay` отвечает за интеграцию и сбор котировок колумбийского лицензированного букмекера Wplay (платформа Playtech).
Цель рефакторинга — переход к расширяемой ООП-архитектуре обработчиков рынков (`WplayMarketHandler`) с поддержкой статистических маркетов (угловые, ЖК), киберспортивных дисциплин (CS2, Dota 2, LoL) и глубокой росписи исходов (BTTS, Double Chance, DNB, тоталы, форы, точный счет).

## Component Structure

```
pro.datawiki.igaming.source.wplay
├── dto
│   ├── WplayEventDto
│   ├── WplayMarketDto
│   └── WplayOutcomeDto
└── service
    ├── WplayOddsMapper (extends AbstractBetTypeMapper, chains WplayMarketHandler)
    └── handler
        ├── WplayMarketContext (DTO контекста события: sportName, sportType, league, teams, isLive)
        ├── WplayMarketHandler (интерфейс стратегии supports/handle)
        ├── AbstractWplayMarketHandler (базовый класс с хелперами resolveScope, extractParam, resolveParam, addOddItem)
        ├── AbstractWplayStatsHandler (базовый класс для статистических маркеров)
        ├── WplayMatchResultHandler (1X2, Moneyline, исходы таймов/периодов)
        ├── WplayTotalHandler (тоталы Over/Under матча, таймов и команд)
        ├── WplayHandicapHandler (европейские и азиатские форы)
        ├── WplayDoubleChanceHandler (двойной шанс 1X, 12, X2)
        ├── WplayBttsHandler (обе забьют Yes/No)
        ├── WplayDrawNoBetHandler (ничья нет ставки / Handicap 0.0)
        ├── WplayCorrectScoreHandler (точный счет матча)
        ├── WplayPeriodHandler (Descanso/Final, Half Time / Full Time)
        ├── WplayStatsCornersHandler (угловые: 1X2, тоталы, форы с StatType.CORNERS)
        ├── WplayStatsCardsHandler (карточки и ЖК с StatType.YELLOW_CARDS)
        └── WplayEsportsHandler (киберспорт: победители карт, тоталы/форы карт и раундов, First Blood, Kills)
```

## Market Handlers & Ordering
1. `@Order(5) WplayEsportsHandler`: Киберспорт (CS2, Dota2, LoL, Valorant).
2. `@Order(10) WplayStatsCornersHandler`: Статистика угловых (`StatType.CORNERS`).
3. `@Order(15) WplayStatsCardsHandler`: Статистика карточек (`StatType.YELLOW_CARDS`).
4. `@Order(30) WplayTotalHandler`: Тоталы Over/Under матча и периодов.
5. `@Order(40) WplayHandicapHandler`: Форы матча и периодов.
6. `@Order(50) WplayDoubleChanceHandler`: Двойной шанс (1X, 12, X2).
7. `@Order(60) WplayBttsHandler`: Обе забьют (Yes/No).
8. `@Order(70) WplayDrawNoBetHandler`: Ничья нет ставки (DNB).
9. `@Order(75) WplayCorrectScoreHandler`: Точный счет матча.
10. `@Order(80) WplayPeriodHandler`: Тайм/Матч (HT/FT).
11. `@Order(100) WplayMatchResultHandler`: Основные исходы 1X2 и Moneyline.

## Verification & Testing
- Комплексное модульное тестирование `WplayOddsMapperTest`:
  - 1X2 и Moneyline исходы;
  - Двойной шанс и ничья нет ставки;
  - Обе забьют;
  - Тоталы (Over/Under матча и индивидуальные тоталы);
  - Форы;
  - Статистика (угловые, ЖК);
  - Киберспорт (победители карт, тоталы карт и раундов);
  - Точный счет и тайм/матч;
  - HTML парсинг и fallback логика.
