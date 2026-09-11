## Why

Currently, several bookmaker crawlers (the BetB2B platform family: 1xBet, 22bet, 888starz, Betandyou, Fansport, Spinbetter, 1xbit, as well as Marathonbet and Zenit) yield significantly fewer matches than their available full line (dozens to hundreds of events instead of 2,000–5,000+).
- **BetB2B Family**: The single generic `Get1x2_Zip` endpoint without explicit sport ID parameters truncates the feed to only popular daily events.
- **Marathonbet**: Opening full Playwright browser instances for each individual league sequentially takes over an hour and leads to timeouts, collecting only the first 1–2 leagues (~18 matches).
- **Zenit**: Requires session warm-up for `imprintHash` cookies and timeline multi-range queries to capture the full catalog.

Scaling these feeds to multi-sport batching and direct category parsing will unlock thousands of additional matches and drastically expand arbitrage surebet yield across the platform.

## What Changes

- **BetB2B Family Multi-Sport Ingestion**: Query `LineFeed/GetSports_Zip` to discover all active sports and pass multi-sport batching (`sports=1,2,3,4,5...` or parallel sport iterations) to `Get1x2_Zip` and `Get1xMatchByLeague`.
- **Marathonbet Category/Bulk Crawling**: Transition from per-league sequential Playwright browser page loads to batched category requests and fast HTTP/JSON parsing.
- **Zenit Imprint Session & Timeline Expansion**: Ensure reliable pre-flight warm-up for `imprintHash` and query full prematch timeline spans.
- **Line Scale Verification**: Verify match throughput across all updated bookmakers in `igaming-dev` / `igaming-master` reaching >= 500–2,000+ events per platform.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Expand crawler ingestion specs to require multi-sport batching for BetB2B clones, bulk category fetching for Marathonbet, and session warm-up for Zenit.

## Impact

- **Affected Modules**: `igaming-source-betb2b`, `igaming-source-1xbet`, `igaming-source-marathonbet`, `igaming-source-zenit`, and `igaming-source-core`.
- **Aggregator & DB**: Significant increase in incoming `odds_actual` volume (up to 800k+ odds and 15k+ matches).
- **K8s & Resources**: Verified against memory limits and non-blocking HikariCP configurations.
