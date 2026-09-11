## Context

Currently, the aggregator pipeline processes incoming bookmaker feeds through TeamUnificationService and MatchUnificationService. If a team or match has not been observed before, it falls back to temporary entity creation and asynchronous LLM normalization (ggregator-normalizer).

Additionally, capture modules (igaming-capture-sofascore, igaming-capture-liveresult) already contain HTTP clients for SofaScore, ESPN, LiveResult, and OpenDota, but are currently used only for post-match score settlement. This design extends them to provide pre-match fixtures and entity catalogs as the authoritative Ground Truth database.

## Goals / Non-Goals

**Goals:**
- Implement MatchFixtureProvider interface in igaming-capture-* to fetch scheduled matches (24-72h ahead) and team profiles.
- Introduce reference fixture correlation in MatchUnificationService using TeamSignature and time-window indexing.
- Automatically register high-confidence TeamAlias mappings whenever a bookmaker event is linked to a reference fixture.
- Store canonical team metadata (logos, English names, localized names, country) during reference fixture ingestion.
- Maintain sub-5ms match resolution throughput for incoming bookmaker odds streams.

**Non-Goals:**
- Scraping live betting odds from sports analytics aggregators (they serve strictly as reference metadata and settlement authorities).
- Replacing the Sport-Polymorphic Regex engine (TeamSignatureFactory), which is utilized directly for signature normalization.

## Decisions

### 1. Unified Reference Fixture Provider Interface
Extend capture modules with a unified MatchFixtureProvider interface:
`java
public interface MatchFixtureProvider {
    boolean supports(Sport sport);
    List<ReferenceFixtureDto> fetchUpcomingFixtures(Sport sport, LocalDate date);
    Optional<TeamProfileDto> fetchTeamProfile(String externalTeamId, Sport sport);
}
`
*Rationale:* Reuses existing HTTP connection pooling, user-agent rotation, and serialization logic in igaming-capture-sofascore and igaming-capture-liveresult.

### 2. Reference Match Correlation Pipeline
When an OddsUpdateRequest arrives:
1. **L1 Fast Cache**: Check Redis/Caffeine (sourceId, externalEventId) -> matchId.
2. **L2 Alias Table**: Resolve 	eam1 and 	eam2 via TeamAliasRepository.
3. **L3 Ground Truth Reference Fixtures**:
   - Parse input names into signatures via TeamSignatureFactory.createSignature(name, sport, source).
   - Query preloaded fixtures in [startTime - 3h, startTime + 3h].
   - If match found, assign canonical Team entities and persist verified TeamAlias for (sourceId, rawName) -> canonicalTeamId.
4. **L4 Fallback**: Create temporary team and enqueue to LLM gateway.

*Alternatives Considered:*
- *Direct on-demand API query to SofaScore during odds processing:* Rejected due to latency (200-500ms network round-trip would block the reactive odds pipeline). Preloading fixtures keeps ingestion latency under 5ms.

### 3. Self-Enriching Alias Graph
Once linked, the bookmaker's raw team name is immediately saved to TeamAlias with the bookmaker's BetSource. Subsequent odds updates for that team hit L1/L2 cache directly without repeating signature parsing or reference queries.

## Risks / Trade-offs

- **[Risk] External Analytics Rate Limiting (HTTP 429)**
  → *Mitigation*: Fixtures are fetched in bulk once every 2-4 hours per sport. Requests are distributed across proxy pool and spaced with jitter.
- **[Risk] Schedule Shifts and Delays (e.g. Tennis Weather Delays)**
  → *Mitigation*: Flexible time matching windows ($\pm 3$ hours for football/hockey, $\pm 24$ hours for tennis tournaments).
- **[Risk] Minor Tournaments Absent in Top Aggregators**
  → *Mitigation*: Graceful fallback to existing LLM Normalization queue for unlisted minor leagues.
