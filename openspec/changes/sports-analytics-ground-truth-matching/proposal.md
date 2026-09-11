## Why

Matching bookmaker matches and teams solely via reactive string heuristics and LLM normalizer introduces latency (1-3s for cold events), cost, and risks of match duplication across disparate bookmaker feeds.

Major sports analytics aggregators (SofaScore, FlashScore/LiveSport, LiveResult, ESPN, OpenDota) maintain complete, authoritative master registries of sports teams, official multilingual aliases, exact fixture schedules, and tournament metadata. Pre-loading scheduled fixtures from these sports analytics providers creates a deterministic **Ground Truth Knowledge Base**. Any incoming bookmaker match can be matched against this master registry in under 5ms, automatically establishing high-confidence TeamAlias links for subsequent odds updates and virtually eliminating LLM overhead.

## What Changes

- **Ground Truth Fixtures Ingestion**: Introduce scheduled synchronization in igaming-capture-* and aggregator modules to preload upcoming matches (fixtures 24-72h ahead) and team entities from SofaScore, FlashScore, LiveResult, and ESPN.
- **Reference-Assisted Match & Team Resolution**: Upgrade MatchUnificationService and TeamUnificationService to match incoming bookmaker events against the pre-loaded Ground Truth calendar using sport-polymorphic signatures and time windows before falling back to temporary entities or LLM queues.
- **Self-Enriching Knowledge Graph**: Automatically create permanent TeamAlias records upon successful match correlation with reference fixtures, caching mappings across all subsequent odds ticks.
- **Enriched Metadata Integration**: Populate canonical logos (logo_url), official English and local names (
ame_english, 
ame_local), country, and league metadata directly from analytics providers into Team and Match entities.

## Capabilities

### Modified Capabilities
- ggregator-core: Introduces Ground Truth Reference Ingestion, Pre-Match Fixture Matcher, and deterministic auto-learning team alias resolution via sports analytics providers.

## Impact

- **aggregator-domain**: Enhanced TeamUnificationService, MatchUnificationService, and queries for reference fixtures and canonical teams.
- **igaming-capture-sofascore** & **igaming-capture-liveresult**: Extended to act not only as post-match settlement providers but also as pre-match fixture & entity sync engines.
- **aggregator-normalizer**: LLM normalization workload drops drastically (>95% reduction), acting strictly as a fallback for obscure/unlisted minor tournaments.
- **Database & Redis**: Enriched canonical teams and instant in-memory cache resolution for bookmaker aliases.
