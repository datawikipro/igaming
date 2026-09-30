# Proposal: #63: [MDM-CORE] Generic Entity Resolution API & UnknownBet Resolver

## Context
- **Plane Task ID**: `f592d6c3-0253-48ed-ae38-96243f6088ea`
- **Module**: `MDM-CORE` (`aggregator-domain`, `aggregator-api`, `igaming-dto`)
- **Downstream Consumers**: 
  - `UI-MDM` (`#49c4d8ef` / `igaming-analytics-service`): Entity Resolution Hub & UnknownBet Radar UI
  - `UI-CRAWLER-OPS` (`#68ef593a`): Ingestion Pipeline Dashboard & Thresholds Monitor

## Problem Statement
When crawlers ingest odds and matches from 52+ bookmakers, variations in entity naming (teams, leagues, markets, and bet outcomes/odds types) frequently lead to unmapped entries. Currently:
1. Unmapped odds types are recorded in `normalization_request` with `type = ODDS_TYPE`, but there is no generic REST API for paginated retrieval, status aggregation, manual resolution, and bulk management.
2. The UI proxy service (`igaming-analytics-service` via `EntityResolutionController` and `UnknownBetRadarController`) expects structured management endpoints under `/api/v1/management/` for:
   - Normalization Queue (`/normalization-queue`, `/stats`, `/{id}/resolve`, `/{id}/retry`, DELETE `/{id}`)
   - Registered Aliases (`/aliases`, DELETE `/{type}/{id}`)
   - UnknownBet Radar (`/unknown-bets`, `/stats`, `/{id}/map`, `/{id}/skip`, `/bookmakers/unknown-summary`)
   - Canonical Dictionary (`/dictionary/canonical-markets`)

## Architectural Solution & Scope

### 1. DTO Model (`igaming-dto`)
Standardized DTOs in package `pro.datawiki.igaming.dto.mdm`:
- `NormalizationQueueItemDto`: Representation of items in the queue with sport, context, status, and retry metrics.
- `NormalizationStatsDto`: Summary statistics across `PENDING`, `RESOLVED` (`COMPLETED`), `FAILED`, `SKIPPED` (`NOT_VALID`).
- `EntityResolutionRequestDto`: Payload for manual resolution specifying canonical name, entity type (`team`, `league`, `market`, `odds_type`, `sport`), and target IDs.
- `EntityAliasItemDto`: Read/delete model for registered aliases.
- `UnknownBetItemDto`: Representation of unmapped bets in UnknownBet Radar.
- `UnknownBetStatsDto` & `BookmakerUnknownSummaryDto`: Aggregated metrics per bookmaker.
- `UnknownBetMapRequestDto`: Mapping payload linking unknown bets to canonical `BetType` codes via `BetTypeRegistry`.
- `CanonicalMarketDto` & `BetTypeRegistry.getCanonicalDictionary()`: Full dictionary of canonical market codes and parametric directions.

### 2. Domain Services (`aggregator-domain`)
- **`NormalizationRequestRepository`**:
  - Paginated queries by status, type, and source ID.
  - Native aggregation query `findBookmakerUnknownSummaries()` for O(1) stats computation across bookmakers.
- **`EntityResolutionService`**:
  - Handles paginated retrieval and filtering of normalization requests.
  - Computes status breakdown metrics.
  - Resolves entities by invoking `EntityAliasManager` (`createTeamAlias`, `createLeague`, `createMarket`, `createOddsTypeAlias`, `createSportAlias`) and updating request status to `COMPLETED`.
  - Manages aliases across `team_alias`, `league_alias`, `market_alias`, and `odds_type_alias`.
- **`UnknownBetResolverService`**:
  - Filters unmapped odds type requests by bookmaker and statuses.
  - Resolves unknown bets to canonical bet codes and generates `OddsTypeAlias`.
  - Marks non-actionable unknown bets as `NOT_VALID` (skipped).
  - Summarizes unmapped volume per bookmaker.

### 3. Management REST API (`aggregator-api`)
- Extends `/api/v1/management` endpoints in `DataManagementController` to provide complete backward-compatible and paginated REST access for both web UI and automated AI workers.
