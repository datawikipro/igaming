# Delta Spec: Aggregator Multi-Bookmaker Balancing and Value Detection

## Added Requirements

### Requirement: Balanced Multi-Bookmaker Overlap
The aggregator core SHALL continuously ingest odds from 20+ distinct bookmakers, maintaining at least 50+ matches with simultaneous multi-bookmaker coverage.

#### Scenario: Real-time surebet detection from multiple feeds
- GIVEN active odds from at least 3 distinct bookmaker sources for a unified match
- WHEN `SurebetDetectorService` executes its scan loop
- THEN it SHALL identify all valid arbitrage combinations and publish them to `surebet_alert`.
