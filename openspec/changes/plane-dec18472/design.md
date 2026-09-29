# Design: [ligastavok] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Architecture & Integration
Модуль `igaming-source-ligastavok` построен на Spring Boot 3.4 и базовых компонентах `igaming-source-core`.
Маппинг рынков и исходов реализован через декомпозицию и иерархию компонентов `AbstractBetTypeMapper`:
- Общие исходы, тоталы и форы (`LigastavokCommonResultMapper`, `LigastavokCommonTotalMapper`, `LigastavokCommonHandicapMapper`).
- Обработка исходов матчей через `LigastavokOddsProcessor` и `BetTypeResolverService`.
- Автоматическая интеграция специализированных видов спорта, киберспорта и статистики (угловые, ЖК) через реестр мапперов ядра.
- Периодическая отправка Heartbeat-сигналов в `igaming-aggregator`.

## Verification & Deployment Strategy
- Сборка OCI-образа через Maven Jib: `100.78.183.101:30500/igaming-source-ligastavok:latest`.
- Валидация тестового пода в K8s с неблокирующим HikariCP и Actuator health probes.
- 5-минутный мониторинг тестового пода (отсутствие Crash/OOM/Fatal).
- Слияние Pull Request #5 в master.
- Обновление k8s-манифеста `igaming-k8s/ligastavok.ru.yaml` и развертывание в namespace `igaming-source`.
- 5-минутный аудит продуктивных подов и проверка наполнения линии матчей.
