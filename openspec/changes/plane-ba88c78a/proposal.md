# Proposal: [capture-injuries] Создать микросервис igaming-capture-injuries для сбора травм и дисквалификаций

## Context
Plane Task ID: `ba88c78a-43c5-49a8-9d70-98af83afc16f`
Target Module: `igaming-source-core`

## Description
**Контекст:** Для прематч-аналитики и квантовой модели критически важно знать отсутствие ключевых игроков (травмы, дисквалификации) за 24–48 часов до матча, не дожидаясь официальных протоколов за 1 час.

**Что нужно сделать:**

- Создать микросервис `igaming-capture-injuries` (Spring Boot 3, неблокирующий старт HikariCP/JPA, Actuator probes).
- Реализовать краулер ключевых источников (Premier Injuries, WhoScored, Transfermarkt).
- Подключить `MdmIngestionClient` для сопоставления игроков и клубов.
- Публиковать канонические списки отсутствующих в топик `sports.canonical.injuries`.
- В `igaming-ai-analyst` учесть травмы лидеров в расчете вероятностей победы и форы.
