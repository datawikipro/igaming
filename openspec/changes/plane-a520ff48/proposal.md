# Proposal: [bet7k] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `a520ff48-bcb3-425b-8c06-be4b778830e6`
- **Bookmaker**: Bet7k (Brazil / LatAm flagship)
- **Target Module**: `igaming-source-bet7k`

## Description
Реализовать модульную архитектуру и ООП-рефакторинг мапперов котировок Bet7k с выделением специализированных обработчиков рынков (паттерн Strategy / Handler):
1. **Киберспорт**: поддержка дисциплин CS2, Dota 2, League of Legends, Valorant (победитель матча/карт, тоталы карт/раундов, форы, First Blood).
2. **Статистика**: угловые удары (`StatType.CORNERS`) и желтые карточки (`StatType.YELLOW_CARDS`).
3. **Основная и расширенная роспись**: 1X2, Двойной шанс, Тоталы (общие/индивидуальные), Форы (европейские/азиатские), Обе забьют (BTTS), Draw No Bet (DNB), Точный счет, Тайм/Матч, Периоды/Таймы.
4. **Архитектурная интеграция**: `Bet7kApiClient`, `Bet7kMatchService`, `Bet7kScheduler`, покрытие модульными тестами `Bet7kOddsMapperTest`.
5. **K8s & CI/CD**: Jib-сборка OCI-образа `100.78.183.101:30500/igaming-source-bet7k:latest`, K8s манифест с неблокирующим стартом HikariCP и Actuator health пробами.
