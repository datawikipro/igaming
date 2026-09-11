## ADDED Requirements

### Requirement: Multi-Sport Batching for BetB2B Family
Crawlers implementing the BetB2B family engine (`1xbet`, `22bet`, `888starz`, `betandyou`, `fansport`, `spinbetter`, `1xbit`) SHALL dynamically discover all available sports and request matches using sport-specific or multi-sport batching to retrieve the full catalog rather than truncating to default daily highlights.

#### Scenario: Full sport tree discovery and batching
- **WHEN** the BetB2B crawler initiates prematch or live discovery
- **THEN** it retrieves active sport IDs from `LineFeed/GetSports_Zip` and requests `LineFeed/Get1x2_Zip` with explicit sport parameters (`sports=1,2,3...` or iterated sport feeds), discovering >= 500–2,000+ matches per bookmaker.

---

### Requirement: Bulk Category Scraping for Marathonbet
The Marathonbet crawler SHALL avoid sequential per-league Playwright browser tab navigation and instead perform bulk category fetching or fast HTTP API extraction to complete line scans within scheduled poll intervals.

#### Scenario: Bulk category event extraction
- **WHEN** the Marathonbet crawler initiates a line scan
- **THEN** it fetches events using batched category endpoints or accelerated HTTP sessions without blocking on per-league browser page creation, completing full line scans in under 120 seconds.

---

### Requirement: Session-Aware Prematch Extraction for Zenit
The Zenit crawler SHALL maintain valid session tokens (such as `imprintHash`) and request full timeline spans to capture the complete prematch line.

#### Scenario: Session warm-up and timeline extraction
- **WHEN** the Zenit crawler starts and discovers lines
- **THEN** it validates or warms up session tokens and queries the full prematch timeline (`timeline=0` or multi-period range), persisting all active matches into `match_cache`.
