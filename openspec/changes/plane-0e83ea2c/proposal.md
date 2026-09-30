# Proposal: [betnacional] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `0e83ea2c-340b-4f62-91ff-ef9bfc0a14dd`
- **Bookmaker**: Betnacional (Brazil / NSX Group / LatAm flagship)
- **Target Module**: `igaming-source-betnacional`

## Description
Реализовать модульную архитектуру и ООП-рефакторинг мапперов котировок Betnacional с выделением специализированных обработчиков рынков (паттерн Strategy / Handler):
1. **Киберспорт**: поддержка дисциплин CS2, Dota 2, League of Legends, Valorant (победитель матча/карт, тоталы карт/раундов, форы, First Blood).
2. **Статистика**: угловые удары (`StatType.CORNERS`) и желтые карточки (`StatType.YELLOW_CARDS`).
3. **Основная и расширенная роспись**: 1X2, Двойной шанс, Тоталы (общие/индивидуальные), Форы (европейские/азиатские), Обе забьют (BTTS), Draw No Bet (DNB), Точный счет, Тайм/Матч, Периоды/Таймы.
4. **Архитектурная интеграция**: `BetnacionalApiClient`, `BetnacionalMatchService`, `BetnacionalScheduler`, покрытие модульными тестами `BetnacionalOddsMapperTest`.
5. **K8s & CI/CD**: Jib-сборка OCI-образа `100.78.183.101:30500/igaming-source-betnacional:latest`, K8s манифест с неблокирующим стартом HikariCP и Actuator health пробами.
