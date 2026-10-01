# Proposal: [wplay] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `860278f3-3300-4f68-97bb-2bf41f77e0a5`
- **Bookmaker**: Wplay Colombia (Playtech sportsbook platform)
- **Target Module**: `igaming-source-wplay`

## Description
Реализовать модульную ООП-архитектуру и глубокий рефакторинг мапперов котировок Wplay Colombia с выделением специализированных обработчиков рынков на базе паттерна Strategy / Market Handler:
1. **Киберспорт**: поддержка киберспортивных дисциплин CS2, Dota 2, League of Legends, Valorant (победитель матча и карт, тоталы карт/раундов/убийств, форы, First Blood).
2. **Статистика**: угловые удары (`StatType.CORNERS`) и карточки/желтые карточки (`StatType.YELLOW_CARDS`).
3. **Основная и расширенная роспись**: 1X2 / Moneyline, Двойной шанс, Тоталы (общие/индивидуальные), Форы (европейские/азиатские), Обе забьют (BTTS), Draw No Bet (DNB -> фора 0.0), Точный счет, Тайм/Матч (HT/FT), Периоды/Таймы.
4. **Архитектурная интеграция**: DTO-модели (`WplayEventDto`, `WplayMarketDto`, `WplayOutcomeDto`, `WplayResponseDto`), рефакторинг `WplayOddsMapper` на инъекцию списка обработчиков `List<WplayMarketHandler>` с сохранением поддержки HTML и DTO.
5. **Тестирование и верификация**: 100% покрытие модульными тестами в `WplayOddsMapperTest`, проверка соответствия Golden Rules проекта (неблокирующий HikariCP, Actuator probes).
