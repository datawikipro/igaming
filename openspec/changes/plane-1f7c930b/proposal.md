# Proposal: [aggregator-enrichment] Интеграция погодных условий для открытых стадионов в sports.canonical.weather

## Context
Plane Task ID: `1f7c930b-bde2-46cf-84e8-d2b645b4d09e`
Target Module: `aggregator-core`

## Description
**Контекст:** Дождь, сильный ветер (> 20 км/ч), снегопад и экстремальная жара существенно влияют на результативность футбольных и открытых теннисных матчей, смещая распределение вероятностей тоталов (Under/Over).

**Что нужно сделать:**

- В `aggregator-enrichment` добавить сервис `WeatherEnrichmentService` с интеграцией OpenWeatherMap API / Weather API.
- Обогащать матчи геокоординатами стадионов и запрашивать прогноз на время матча.
- Публиковать погодную сводку в топик `sports.canonical.weather` и сохранять в `match.weather_json`.
