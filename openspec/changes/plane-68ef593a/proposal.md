# Proposal: [UI-CRAWLER-OPS] Ingestion Pipeline Dashboard & Thresholds Monitor

## Context
Plane Task ID: `68ef593a-5215-43e5-900b-1a36b92eb04a`  
Sequence ID: 65 (depends on sequence 63 `[MDM-CORE] Generic Entity Resolution API & UnknownBet Resolver`)  
Component: `igaming-analytics-service` / Operator UI

## Why
SmartBet.guru aggregates live and pre-match odds from 52+ sportsbooks. To ensure uninterrupted operation and identify broken scrapers before they impact arbitrage (+EV) calculations, the operations team requires a centralized real-time dashboard. 

Crucially, **Golden Rule #8** of the platform dictates:
> "Критерий наполнения линии (Threshold >= 500 матчей): Задача по любому букмекеру считается выполненной ТОЛЬКО при наполнении линии от 500 активных матчей (`SELECT count(*) FROM match_cache >= 500`). Статус `Running 1/1` у пода при 0 матчей в БД считается незавершённым дефектом."

Without a dedicated Ingestion Pipeline Dashboard & Thresholds Monitor, operators must manually inspect multiple databases, logs, and Kafka offsets to evaluate scraper health and threshold compliance.

## What Changes
1. **Crawler Operations & Ingestion Backend (`igaming-analytics-service`)**:
   - `CrawlerOpsController` (`/api/v1/crawler-ops`):
     - `GET /api/v1/crawler-ops/fleet` — real-time bookmaker fleet metrics, proxy routes, and Golden Rule #8 statuses.
     - `POST /api/v1/crawler-ops/fleet/refresh` — instantaneous cache invalidation and fleet re-evaluation.
     - `GET /api/v1/crawler-ops/pipeline/stats` — end-to-end ingestion pipeline health, Kafka message throughput, active odds and matches counts, pending normalizations.
     - `GET /api/v1/crawler-ops/thresholds` — detailed per-bookmaker threshold compliance table against the >= 500 match standard with progress percentages, delay freshness, and sports breakdowns.
     - `POST /api/v1/crawler-ops/crawlers/{id}/probe` — instant health check and probe dispatch for targeted bookmaker.
   - `MdmUiController`:
     - Added routing for `/crawler-ops`, `/crawler-ops/`, `/pipeline`, `/pipeline/` directing operators to the Ingestion Pipeline Dashboard.

2. **Ingestion Pipeline Dashboard & Thresholds Monitor UI (`crawler-ops-dashboard.html`)**:
   - High-contrast dark operator UI in theme with SmartBet.guru operations console.
   - **Executive KPI Cards**: Fleet size & online status, Golden Rule #8 compliance percentage, Ingestion flow throughput, and Stale/Degraded alerts counter.
   - **Interactive Visual Pipeline Flow Diagram**:
     - Visual representation of the stages: `[52+ Bookmaker Crawlers / Loaders]` ➔ `[Kafka: odds.updates]` ➔ `[aggregator-ingestion]` ➔ `[PostgreSQL: odds_actual]` ➔ `[aggregator-surebet Scanner]`.
     - Real-time latency, throughput, and status per stage.
   - **Thresholds Monitor (Golden Rule #8 Table)**:
     - Search & multi-filter by compliance status (`COMPLIANT >=500`, `DEGRADED <500`, `CRITICAL DEFECT: 0 MATCHES`), delay (`>5m`), and proxy route (`DIRECT`, `OUTLINE_US`, `OUTLINE_VPN_EU_NL`).
     - Visual filling progress bar for the 500-match goal.
     - Drill-down drawer displaying sports distributions and top leagues for each bookmaker.
     - Auto-refresh toggle (5s, 10s, 30s) and manual refresh with loading feedback.
   - Unified cross-navigation between Entity Resolution Hub (`/mdm`) and Ingestion Pipeline (`/crawler-ops`).

## Capabilities
### Modified Capabilities
- `crawler-engine`: Added automated threshold evaluation, real-time pipeline visualization, and crawler probe APIs for operational fleet governance.

## Impact
- **Services**: `igaming-analytics-service` (new `CrawlerOpsController`, UI routing), `igaming-aggregator` (proxied fleet and diagnostics APIs).
- **Users**: DevOps, Platform Reliability Engineers, and AI Developer workers monitoring scraper lines.
