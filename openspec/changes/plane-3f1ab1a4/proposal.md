# Proposal: #39: [paf] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `3f1ab1a4-81d9-454f-bc67-0e19f0da66ec`
- **Bookmaker**: PAF (`igaming-source-paf`, Åland Gaming / Paf.com на платформе Kambi)
- **Target Module**: `igaming-source-paf`

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-paf` (букмекер PAF на платформе Kambi, Финляндия/ЕС):
1. **Модульная ООП-архитектура обработчиков рынков (`PafMarketHandler`)**:
   - Переход от монолитных мапперов к цепочке специализированных хэндлеров по паттерну Strategy / Chain of Responsibility с сортировкой по приоритету (`@Order`).
   - Поддержка контекста вида спорта (`SportType`) и нормализация рынков на разных языках (English, Finnish, Swedish).
2. **Наследование базовых абстракций `AbstractBetTypeMapper`**:
   - Интеграция с `igaming-source-core` и каноническими интерфейсами резолвинга ставок (`map`, `supports`).
3. **Расширение росписи исходов**:
   - Основные исходы (1X2, Moneyline 2-way/3-way, таймы/периоды/четверти/сеты) в `PafMoneylineHandler`;
   - Двойной шанс (1X, 12, X2 матча и 1-го тайма, мультиязычные критерии English/Finnish/Swedish) в `PafDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No матча и 1-го тайма) в `PafBttsHandler`;
   - Ничья нет ставки (DNB матча и 1-го тайма -> фора 0.0) в `PafDrawNoBetHandler`;
   - Тоталы (Over/Under матча, таймов и индивидуальные) в `PafTotalHandler`;
   - Форы (Handicap / Asian Handicap матча и таймов) в `PafHandicapHandler`.
4. **Поддержка статистических рынков**:
   - `AbstractPafStatsHandler` — базовый класс для статистических рынков;
   - `PafStatsCornersHandler` — угловые (исходы 1X2, тоталы, форы с `StatType.CORNERS`);
   - `PafStatsCardsHandler` — желтые карточки и предупреждения (исходы 1X2, тоталы, форы с `StatType.YELLOW_CARDS`).
5. **Поддержка киберспорта (`PafEsportsHandler`)**:
   - Дисциплины CS2, Dota 2, League of Legends, Valorant и др.;
   - Победители отдельных карт (`BetScope.MAP_1` .. `BetScope.MAP_5`);
   - Тоталы и форы по картам (`StatType.MAPS`);
   - Тоталы и форы по раундам на картах (`StatType.ROUNDS`).
6. **Всестороннее модульное тестирование**:
   - Комплексные unit-тесты в `PafOddsMapperTest`, покрывающие основные рынки, роспись, статистику, киберспорт и граничные случаи (пустые/невалидные данные, fallback).
7. **Инфраструктурная совместимость**:
   - Регистрация модуля в корневом `pom.xml`, неблокирующий старт HikariCP/JPA (`initialization-fail-timeout=0`), Actuator health readiness/liveness пробы.
