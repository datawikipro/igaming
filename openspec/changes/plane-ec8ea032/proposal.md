# Proposal: [10bet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `ec8ea032-f4e0-41b9-b7d9-76b7a2c354bf`
- **Bookmaker**: 10bet (International Tier-1 / SBTech sportsbook)
- **Target Module**: `igaming-source-10bet`

## Description
Реализовать модульную ООП-архитектуру и рефакторинг мапперов котировок 10bet с выделением специализированных обработчиков рынков (паттерн Strategy / Market Handler):
1. **Киберспорт**: поддержка дисциплин CS2, Dota 2, League of Legends, Valorant (победитель матча/карт, тоталы карт/раундов, форы, First Blood).
2. **Статистика**: угловые удары (`StatType.CORNERS`) и желтые карточки (`StatType.YELLOW_CARDS`).
3. **Основная и расширенная роспись**: 1X2, Двойной шанс, Тоталы (общие/индивидуальные), Форы (европейские/азиатские), Обе забьют (BTTS), Draw No Bet (DNB), Точный счет, Тайм/Матч, Периоды/Таймы.
4. **Архитектурная интеграция**: `TenBetApiClient`, `TenBetMatchService`, `TenBetScheduler`, покрытие модульными тестами `TenBetOddsMapperTest`.
5. **K8s & CI/CD**: Jib-сборка OCI-образа `100.78.183.101:30500/igaming-source-10bet:latest`, K8s манифест с неблокирующим стартом HikariCP и Actuator health пробами.
