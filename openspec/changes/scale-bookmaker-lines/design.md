## Context

Current crawler implementations in `igaming-source-betb2b`, `igaming-source-marathonbet`, and `igaming-source-zenit` encounter structural bottlenecks that restrict match discovery volume:
- **BetB2B Family**: Default `Get1x2_Zip` queries lack sport parameters, causing the upstream API to truncate events to daily highlights (~50–250 matches).
- **Marathonbet**: Opening Playwright browser contexts for each individual league in sequence incurs high latency (>10s per league), timing out before crawling the full hierarchy.
- **Zenit**: Single un-parameterized calls without refreshed imprint headers or timeline iterations miss broader tournament listings.

## Goals / Non-Goals

**Goals:**
- Implement sport-level multi-batching in `AbstractBetB2BFamilyApiClient` to discover and ingest the full sport tree (football, hockey, tennis, basketball, esports, etc.) for all 8 BetB2B clones.
- Optimize Marathonbet discovery using bulk category requests and high-speed HTTP retrieval.
- Ensure Zenit session warm-up reliably injects `imprintHash` and queries multi-timeline ranges.
- Validate that each affected bookmaker achieves >= 500–2,000+ matches in the aggregator.

**Non-Goals:**
- Modifying odds normalization mapping logic in `AbstractBetTypeMapper` (already complete).
- Changing aggregator core data structures or REST API schemas.

## Decisions

### Decision 1: BetB2B Dynamic Sport Discovery & Multi-Sport Batching
- **Approach**: In `AbstractBetB2BFamilyApiClient`, query `LineFeed/GetSports_Zip?lng=ru` to retrieve the list of active sport IDs. Then batch sport IDs into multi-sport queries (`sports=1,2,3,4,5...`) or query major sports in parallel.
- **Alternatives Considered**: Querying each championship individually (`GetChamps_Zip`) — too many HTTP roundtrips; multi-sport `Get1x2_Zip` delivers thousands of games in 2–3 requests.

### Decision 2: Marathonbet Direct HTTP / Fast-Category Parsing
- **Approach**: Use fast HTTP / JSON endpoints for category lists or extract whole sport tables in single requests instead of launching separate Playwright browser page contexts for every single league.
- **Alternatives Considered**: Increasing Playwright pool size — consumes excessive RAM and triggers browser concurrency limits on worker nodes.

### Decision 3: Zenit Imprint Cookie Pre-Flight
- **Approach**: Ensure `ZenitApiClient` executes a lightweight pre-flight request to `https://zenit.win/` to populate `imprintHash` before triggering the main line printer API.

## Risks / Trade-offs

- **[Risk] Upstream Rate Limiting**: Sending too many rapid requests to BetB2B or Marathonbet could trigger temporary HTTP 429/403.
  - **Mitigation**: Batch multiple sport IDs into single requests (`sports=1,2,3,4,5...`) and maintain 30–60 second poll intervals with Jitter.
- **[Risk] Increased Aggregator DB Load**: 10x increase in incoming matches and odds.
  - **Mitigation**: Aggregator `odds_actual` uses indexed upserts with optimistic lock handling and non-blocking HikariCP connections.
