## ADDED Requirements

### Requirement: Conflict-Free Multi-Bookmaker Upsert and Scheduled Scanning
The aggregator pipeline SHALL persist incoming odds into PostgreSQL `odds_actual` using expression-based unique indexing without constraint collisions, and continuously execute periodic Surebet and Valuebet scans across all matched multi-bookmaker events.

#### Scenario: High-throughput odds actual upsert
- **WHEN** concurrent odds update messages for the same match and market are received from Kafka
- **THEN** the ingestion worker executes an atomic native upsert matching the composite unique constraint `(match_id, bet_source_id, odds_type_id, COALESCE(param, -10000.0), COALESCE(outright_selection_id, -1))` without throwing SQLGrammarException.

#### Scenario: Periodic surebet scanning
- **WHEN** new odds are persisted or updated in `odds_actual`
- **THEN** `SurebetScanScheduler` periodically triggers `SurebetDetectorService` and `ValueBetFinderService` to evaluate cross-bookmaker arbitrage across updated match IDs.
