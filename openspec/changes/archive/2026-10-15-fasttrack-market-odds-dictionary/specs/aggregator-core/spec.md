# Aggregator Core Specification

## ADDED Requirements

### Requirement: FastTrack In-Memory Odds Resolution
The odds normalization and ingestion pipeline MUST resolve common market outcomes, match results (1X2), double chances, both teams to score (BTTS), and standard totals in-memory via `FastTrackDictionary` with $O(1)$ complexity prior to database fallback, preventing temporary database record churn and unnecessary LLM gateway invocations.

#### Scenario: In-memory resolution of standard betting outcomes
- **WHEN** an odds update with common outcomes (e.g., 1X, X2, 12, BTTS, Over/Under, Win1/Win2) is received without a pre-existing alias
- **THEN** `FastTrackDictionary` SHALL resolve the raw outcome to its canonical `BetType` in memory without triggering background LLM normalization requests or generating temporary `TEMP_` odds types.

#### Scenario: Sub-millisecond dictionary lookup performance
- **WHEN** sports or odds names are resolved
- **THEN** lookups SHALL execute in $O(1)$ time complexity using pre-indexed case-insensitive lookup tables and pre-compiled regex patterns.
