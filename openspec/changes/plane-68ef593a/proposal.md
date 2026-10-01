# Proposal: [UI-CRAWLER-OPS] Ingestion Pipeline Dashboard & Thresholds Monitor

## Context
- **Plane Task ID**: `68ef593a-5215-43e5-900b-1a36b92eb04a`
- **Module**: `UI-CRAWLER-OPS` (`igaming-analytics-service`)
- **Dependencies**: Depends on sequence_id 63 (`[MDM-CORE] Generic Entity Resolution API & UnknownBet Resolver`)
- **Target Branch**: `feature/plane-68ef593a`

## Background & Problem Statement
Ecosystem SmartBet.guru operates 52 bookmaker crawlers and loaders continuously feeding live and prematch sports betting lines into Kafka and PostgreSQL `match_cache`. 

Under **Rule 8 (Threshold >= 500 active matches)** of `AGENTS.md`:
> "Задача по любому букмекеру считается выполненной ТОЛЬКО при наполнении линии от 500 активных матчей (`SELECT count(*) FROM match_cache >= 500`). Статус `Running 1/1` у пода при 0 матчей в БД считается незавершённым дефектом."

Previously, platform operators and AI developers lacked a centralized, real-time ingestion operations dashboard to:
1. Track whether each of the 52 crawlers is actively ingesting odds and fulfilling the 500-match threshold.
2. Observe ingestion pipeline metrics: throughput (events/sec), parser latency, network lag, and error rates across RU (ЦУПИС/ЕРАИ), EU/Offshore, and US bookmakers.
3. Rapidly detect stale crawlers, stalled ingestion, or drops in line volume before downstream surebet and EV calculators starve.
4. Trigger on-demand sync/crawl passes directly from an operations UI without manual kubectl intervention.

## Proposed Solution
Implement a dedicated **Ingestion Pipeline Dashboard & Thresholds Monitor** within the centralized operations microservice `igaming-analytics-service`:

1. **Backend REST APIs (`CrawlerOpsController`, `CrawlerOpsService`)**:
   - `GET /api/v1/crawler-ops/pipelines`: Detailed status and metrics for all 52 bookmaker pipelines.
   - `GET /api/v1/crawler-ops/pipelines/stats`: High-level aggregated statistics (total crawlers, threshold pass/fail counts, total matches, throughput).
   - `GET /api/v1/crawler-ops/thresholds` & `POST /api/v1/crawler-ops/thresholds`: Threshold rules configuration (default 500 matches, warning 300, stale timeouts).
   - `POST /api/v1/crawler-ops/pipelines/{bookmaker}/sync`: Trigger immediate on-demand crawl pass.
   - `GET /api/v1/crawler-ops/alerts`: Real-time threshold violation and crawler health alerts.

2. **Frontend UI (`crawler-ops-dashboard.html`)**:
   - High-density dark operations interface served at `/crawler-ops` and `/ingestion-pipeline`.
   - Real-time auto-refresh with status cards, progress bars towards the 500-match threshold, regional filters (RU TSUPIS, EU/Offshore, US), and instant manual sync triggers.
   - Cross-linking with `/mdm` (Entity Resolution Hub) for unified infrastructure operations.
