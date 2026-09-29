# Design: [pinnacle] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-pinnacle` построен на Spring Boot 3.4 и базовых компонентах `igaming-source-core`.
Маппинг рынков и исходов декомпозирован по ООП-принципам:
- Базовый абстрактный обработчик: `AbstractPinnacleMarketHandler`.
- Специализированные обработчики исходов:
  - `PinnacleMoneylineHandler` (1X2, Moneyline)
  - `PinnacleSpreadHandler` (Азиатские и европейские форы)
  - `PinnacleTotalHandler` (Общие тоталы больше/меньше)
  - `PinnacleTeamTotalHandler` (Индивидуальные тоталы команд)
  - `PinnacleBttsHandler` (Обе забьют)
  - `PinnacleDoubleChanceHandler` (Двойной шанс 1X, X2, 12)
  - `PinnacleDrawNoBetHandler` (Ничья нет ставок)
- Киберспорт и статистика обрабатываются через `PinnacleOddsMapper` и реестр мапперов ядра.
- Автоматическая передача котировок в `igaming-aggregator`.

## Verification & Deployment Strategy
- Сборка OCI-образа через Maven Jib: `100.78.183.101:30500/igaming-source-pinnacle:latest`.
- Запуск тестового пода `igaming-source-pinnacle-test` в namespace `igaming-dev`.
- 5-минутный soak-тест тестового пода с мониторингом логов и Actuator health probes.
- Слияние PR #10 в master.
- Развертывание в продуктивном окружении (namespace `igaming-source`).
- 5-минутный аудит продуктивных подов и проверка наполнения линии матчей (`SELECT count(*) FROM match_cache`).
