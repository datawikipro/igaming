# Crawler Engine Specification Delta

## Requirements

### Requirement: Full-Depth Feed Ingestion & Dynamic Scheduling
Crawlers must ingest full market depth (including Totals, Handicaps, BTTS, Double Chance, and Player Props) and configure league processing concurrency capable of sweeping entire sports catalogs within cache freshness windows.

#### Scenario: BetB2B family multi-market ingestion
- **WHEN** BetB2B crawlers (1xBet, FanSport, Melbet, SpinBetter, 22Bet) process sports lines
- **THEN** they ingest both 1X2 and extended sub-market rosters across championships without restricting to top showcase matches.

#### Scenario: Betcity full prematch event roster
- **WHEN** Betcity crawler fetches prematch sports data
- **THEN** the API query includes `add=dep_event&template=1` to retrieve full odds rosters for all scheduled events.

#### Scenario: Marathonbet high-throughput league batching
- **WHEN** Marathonbet crawler processes pending leagues from `league_cache`
- **THEN** it executes concurrent league batches (minimum 15-25 leagues per cycle) to guarantee full catalog freshness.

#### Scenario: Dynamic deep-link generation
- **WHEN** odds update requests are constructed for any bookmaker
- **THEN** the `eventUrl` is built with dynamic sport, tournament, and event identifiers without hardcoded placeholder strings.
