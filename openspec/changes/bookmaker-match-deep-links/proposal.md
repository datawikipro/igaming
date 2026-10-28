## Why

Currently, matches in MDM and the aggregator lack unified persistence and presentation of direct, openable bookmaker and ground-truth (SofaScore) match URLs. When users browse a match on SmartBet.guru, they cannot easily jump directly to that match on specific bookmaker websites or SofaScore. Crawlers occasionally emit partial or missing event URLs. This change persists bookmaker match URLs in the database, enforces openable URL generation across all crawlers, and renders direct links on the match card and odds table.

## What Changes

- **Crawler URL Enforcement**: Ensure all crawlers (`igaming-source-*`) and data capture sources (`igaming-capture-sofascore`) construct and send valid, openable URLs (`eventUrl`) for both pre-match and live events, and persist them in `match_cache.event_url`.
- **Match Deep Links Persistence in DB**: Add `bookmaker_urls` (JSON/map) to `mdm_match` and `match_record` to store all known bookmaker and SofaScore links per match.
- **DTO & Redis Integration**: Expose `bookmaker_urls` in `CanonicalMatchHeaderDto`, and update them in Redis alongside odds updates.
- **API Aggregation**: In `igaming-portal` (`PortalMatchController`), enrich `GET /api/v1/matches/{id}` with `bookmakerUrls` from DB, active Redis odds, and SofaScore.
- **Frontend Match Card UI**: Display an interactive "Bookmaker & Source Links" group with bookmaker logos and open icons in `MatchDetailHeader` / match details, and make bookmaker rows in `MatchOddsGrid` clickable to open the match directly in a new tab.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Enforce openable event URL generation in crawlers and persistence in crawler cache.
- `aggregator-core`: Persist and fold bookmaker URLs into `mdm_match`, `match_record`, and `CanonicalMatchHeaderDto`.
- `portal-gateway`: Deliver `bookmakerUrls` via `/api/v1/matches/{id}`.
- `frontend-architecture`: Render openable bookmaker and SofaScore links on the match card and odds grid.

## Impact

- `igaming-source-*`: Crawlers ensure `eventUrl` is properly set in `MatchCache` and `OddsUpdateRequest`.
- `igaming-capture-sofascore`: Exposes direct SofaScore match URL (`https://www.sofascore.com/event/{id}`).
- `igaming-dto`: `CanonicalMatchHeaderDto` updated with `bookmakerUrls`.
- `igaming-mdm-service`: `MdmMatch` updated with `bookmaker_urls` column and resolution logic.
- `aggregator-domain`: `Match` updated with `bookmaker_urls` column and update logic in `OddsUpsertManager` / `MatchAggregatorService`.
- `igaming-portal`: `PortalMatchController` enriches match response with `bookmakerUrls`.
- `smartbet.guru` (frontend): `MatchDetailHeader.tsx`, `MatchOddsGrid.tsx`, and supporting components display clickable bookmaker links.
