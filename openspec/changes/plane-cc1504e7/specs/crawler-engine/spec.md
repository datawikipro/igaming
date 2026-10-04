# Crawler Engine Specification Delta (plane-cc1504e7)

## Purpose
Specifies market scope isolation, statistical outcome segregation, and high-throughput factor deduplication for the BetB2B bookmaker family (including FanSport) within the crawler and loader ingestion engine.

## ADDED Requirements

### Requirement: BetB2B Market Scope & Statistical Market Segregation
The BetB2B modular mapping subsystem (`XbetFamilyMapper` and associated `XbetFactorStrategy` implementations) must strictly segregate main match outcomes from period/half scopes and specialized statistical categories (such as corners and yellow cards) to prevent cross-scope and cross-market contamination.

#### Scenario: Full match vs half period outcome isolation
- **WHEN** raw BetB2B odds for half-time markets (e.g. factors 15..17 for 1H 1X2, or factor 45 for 1H Total Over 1.5) are processed alongside full-match factors (1..3 for 1X2, 9..10 for Totals)
- **THEN** outcomes are assigned strictly distinct scopes (`BetScope.HALF_1` vs `BetScope.FULL_MATCH`) and must not cross-contaminate.

#### Scenario: Statistical market isolation
- **WHEN** statistical factors (e.g. corner factors 1707..1712, card factors 1738..1743) are received from the FanSport / BetB2B raw line
- **THEN** outcomes are tagged with their specific `StatType` (`StatType.CORNERS`, `StatType.YELLOW_CARDS`) and never conflated with standard match totals or match handicaps (`StatType.MATCH`).

---

### Requirement: Raw Feed Duplicate Factor Collision Suppression
The ingestion odds processor (`AbstractOddsProcessor`) must detect and safely suppress duplicate or colliding factors present in raw bookmaker event payloads without failing the batch or propagating corrupted odds to downstream aggregator queues.

#### Scenario: Duplicate outcome factor collision handling
- **WHEN** a bookmaker feed produces multiple identical outcome factor identifiers for a single event (e.g. duplicate W1 or identical total thresholds)
- **THEN** the processor logs a critical duplicate mapping collision warning, retains the first deterministic valid outcome, suppresses subsequent colliding duplicates, and preserves full batch processing continuity.
