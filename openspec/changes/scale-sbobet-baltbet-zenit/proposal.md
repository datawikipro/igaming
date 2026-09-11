# OpenSpec Proposal: `scale-sbobet-baltbet-zenit`

## 1. Problem Statement

Three target bookmakers in the SmartBet.guru ecosystem exhibit low match volumes and odds yields:
- **SBOBET**: Scraping was constrained to the `/ru-RU/euro/<sport>` path which returns only today's featured matches (~200 games). The Early Market containing 85%+ of future events was not queried, and only 5 sports were polled.
- **Baltbet**: An N+1 synchronous HTTP bottleneck executed `fetchGroupingEvent(externalId)` sequentially for up to 1,500 games (8–10 minutes total delay), causing scheduler timeouts and dropped matches.
- **Zenit**: Heavy browser automation timeouts over proxy connections and a slow 12-minute prematch polling interval suppressed line ingestion.

## 2. Proposed Changes

- **SBOBET**:
  - Add Early Market URL parameter (`?od=all`) with fallback.
  - Expand sport mappings to 17 sports in `SPORT_URL_SEGMENTS` and `TARGET_SPORTS`.
- **Baltbet**:
  - Introduce parallel async batch enrichment using a 25-thread worker pool and local in-memory team name cache.
  - Reduce team enrichment latency from 10 minutes to 3–5 seconds.
- **Zenit**:
  - Prioritize lightweight HTTP GET with full Ajax headers over heavy Playwright navigation.
  - Reduce prematch and live polling intervals to 60s and 30s.

## 3. Success Criteria

- All three bookmaker crawlers and loaders build cleanly with Jib on `ghcr.io/datawikipro/igaming-source-base:latest`.
- SBOBET, Baltbet, and Zenit reach 1,000–3,000+ matches in aggregator `odds_actual`.
- Zero errors over a 5-minute continuous run in Kubernetes.
