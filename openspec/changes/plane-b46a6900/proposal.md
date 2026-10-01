# Proposal: [betesporte] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- **Plane Task ID**: `b46a6900-70b5-4a7e-bd6a-486da9408659`
- **Bookmaker**: Betesporte (Brazil / LatAm Altenar platform)
- **Target Module**: `igaming-source-betesporte`

## Description
Реализовать модульную архитектуру и ООП-рефакторинг мапперов котировок Betesporte с выделением специализированных обработчиков рынков (паттерн Strategy / Handler):
1. **Киберспорт**: поддержка дисциплин CS2, Dota 2, League of Legends, Valorant (победитель матча/карт, тоталы карт/раундов, форы, First Blood).
2. **Статистика**: угловые удары (`StatType.CORNERS`) и желтые карточки (`StatType.YELLOW_CARDS`).
3. **Основная и расширенная роспись**: 1X2, Двойной шанс, Тоталы (общие/индивидуальные), Форы (европейские/азиатские), Обе забьют (BTTS), Draw No Bet (DNB), Точный счет, Тайм/Матч, Периоды/Таймы.
4. **Архитектурная интеграция**: `BetesporteApiClient`, `BetesporteMatchService`, `BetesporteScheduler`, покрытие модульными тестами `BetesporteOddsMapperTest`.
5. **K8s & CI/CD**: Jib-сборка OCI-образа `100.78.183.101:30500/igaming-source-betesporte:latest`, K8s манифест с неблокирующим стартом HikariCP и Actuator health пробами.
