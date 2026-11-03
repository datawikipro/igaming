# Proposal: [capture-fbref] Реализовать Kafka-стриминг xG и shot maps в топики sports.canonical.xg и sports.canonical.shots

## Context
Plane Task ID: `48a83be3-da4b-434e-b41d-38d625c5ffe3`
Target Module: `monorepo`

## Description
**Контекст:** В настоящее время сервис `igaming-capture-fbref` сохраняет продвинутую статистику FBref (xG, xAG, SCA) и карты ударов Understat (x, y координаты, тип удара) исключительно в локальные таблицы PostgreSQL (`fbref_raw_reports`, `understat_raw_shots`). В Kafka отправляются только аномалии в `mdm.anomaly.errors`.

**Что нужно сделать:**

- В модуле `igaming-dto` разработать DTO `CanonicalMatchXgDto` и `CanonicalShotMapDto`.
- В `igaming-capture-fbref` настроить KafkaProducer и публикацию успешных отчетов в топики `sports.canonical.xg` и `sports.canonical.shots`.
- В `igaming-ai-analyst` реализовать `ReactiveXgKafkaConsumer` для реактивного обогащения досье матча и тактических тензоров без тяжелых SQL-джойнов.
- Проверить компиляцию Maven и валидность передачи данных.
