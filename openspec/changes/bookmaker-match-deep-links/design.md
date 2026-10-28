## Context

See `proposal.md` for background and motivation. Currently, `CanonicalMatchEventDto` and `OddsUpdateRequest` already support `eventUrl`, and `RedisFlatOdd` stores `eventUrl` per bookmaker, but neither `MdmMatch` nor `Match` records persist a consolidated map of bookmaker deep links. Furthermore, some crawlers only generate `eventUrl` during odds mapping rather than on initial `match_cache` conversion.

## Goals / Non-Goals

**Goals:**
- Provide full match deep links in the database (`mdm_match.bookmaker_urls` and `match_record.bookmaker_urls`).
- Ensure crawlers populate valid, openable match URLs on both `MatchCache` and `OddsUpdateRequest`.
- Dynamically merge URLs in `PortalMatchController` from DB records, active Redis odds, and SofaScore.
- Render prominent, responsive bookmaker deep-link buttons in `MatchDetailHeader` and linkify bookmaker rows in `MatchOddsGrid`.

**Non-Goals:**
- Scraping behind bookmaker authentication or requiring login cookies.
- Automated bet slip placement (handled by Chrome extension or separate modules).

## Decisions

### Decision 1: JSON-serialized `bookmaker_urls` map on Match entities
Store a JSON map of `{"bookmakerCode": "https://..."}` in `mdm_match.bookmaker_urls` and `match_record.bookmaker_urls`.
- *Rationale*: Flexible, non-intrusive schema extension that handles arbitrary numbers of bookmakers and score providers without N+1 join overhead.
- *Alternatives considered*: A separate relational table `match_bookmaker_link`. Rejected because it introduces unnecessary joins on high-throughput match queries.

### Decision 2: Multi-layer URL resolution (DB + Redis + Ground Truth)
In `PortalMatchController`, when serving `/api/v1/matches/{id}`, aggregate URLs from:
1. Canonical match header (`bookmaker_urls`).
2. SofaScore ground truth (`https://www.sofascore.com/event/{sofascoreId}`).
3. Active Redis odds (`eventUrl` from each distinct bookmaker in `findFlatOddsByMatchId`).
- *Rationale*: Immediate 100% link coverage even for matches created prior to schema rollout.

### Decision 3: Frontend Deep Links UI Presentation
1. In `MatchDetailHeader`: Add a sleek "Bookmakers & Ground Truth" pill bar featuring provider logos/names and external link indicators (↗).
2. In `MatchOddsGrid`: Render the bookmaker name in the odds grid table as an openable link with external link icon.
- *Rationale*: Intuitive UX allowing one-click access both from the match header overview and directly while analyzing odds differences in the table.

## Risks / Trade-offs

- [Risk: Bookmaker changes URL path structure] → Centralize sanitization and URL resolution in `BookmakerRegistry` (TypeScript) and `BookmakerUrlUtils` (Java) with fallback to base betting domains.
- [Risk: Concurrency on JSON updates] → Use thread-safe map mergers in `MatchAggregatorService` and `MdmMatchResolutionService`.
