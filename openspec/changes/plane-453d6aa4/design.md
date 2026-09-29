# Design: [leon] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-leon` построен на Spring Boot 3.4 и `igaming-source-core`.
Маппинг котировок выполняется через иерархию компонентов `AbstractBetTypeMapper` и динамических резолверов `DynamicBetTypeMapper`:
- Футбольные исходы, тоталы, форы и спецмаркеты (`FootballResultMapper`, `FootballTotalMapper`, `FootballHandicapMapper`, `FootballSpecialMapper`).
- Статистические маркеты (угловые `StatType.CORNERS`, желтые карточки `StatType.YELLOW_CARDS`).
- Киберспортивные рынки (`EsportsResultMapper`, `EsportsTotalMapper`, `LeonSetsMapsFramesDynamicBetTypeMapper`, `LeonEsportsRoundsDynamicBetTypeMapper`).
- Общие рынки (`LeonCommonResultMapper`, `LeonCommonTotalMapper`, `LeonCommonHandicapMapper`, `LeonCommonOddEvenMapper`, `LeonCommonCorrectScoreMapper`).

## Verification & Deployment Strategy
- Maven Jib сборка в `ghcr.io/datawikipro/igaming-source-leon:latest`.
- Запуск тестового пода с неблокирующим HikariCP и Actuator health probes.
- 5-минутный мониторинг тестового пода (отсутствие Exception/NPE/Crash).
- Merge PR #8 в master.
- Обновление прод-развертывания (`igaming-source-leon-crawler`, `igaming-source-leon-loader`).
- Финальный 5-минутный аудит и проверка порога активных матчей >= 500.
