# Proposal: [aggregator-enrichment] Реактивная публикация видеотрансляций в Kafka топик sports.broadcasts.discovered

## Context
Plane Task ID: `3938ba6d-f955-4ed0-bce1-99bfb663ed11`
Target Module: `aggregator-core`

## Description
**Контекст:** `MatchBroadcastDiscoveryService` опрашивает ScoreBat API и резолвит открытые стримы (Twitch, YouTube, MatchTV, Winline TV) раз в 5 минут по крону и напрямую обновляет `match.broadcasts_json` в БД.

**Что нужно сделать:**

- При обнаружении или обновлении стримов для матча генерировать и отправлять событие `MatchBroadcastDto` в Kafka топик `sports.broadcasts.discovered`.
- Обеспечить немедленную доставку информации о доступном плеере на фронтенд через шину событий без ожидания 5-минутного интервала.
- Соблюдать Zero Server Bandwidth: доставка только метаданных плееров и embed iframe.
