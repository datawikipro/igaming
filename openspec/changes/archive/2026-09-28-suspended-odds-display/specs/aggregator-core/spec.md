## ADDED Requirements

### Requirement: Suspended Odds Lifecycle and Arbitrage Exclusion
The system SHALL detect suspended or withdrawn bookmaker odds and strictly exclude them from all surebet, value bet, and middle opportunity evaluations, while retaining their last observed price in storage for line-movement and historical analytics.

#### Scenario: Live match factor omitted from crawler feed
- **WHEN** a bookmaker crawler update arrives for a live match where previously active outcome factors are omitted or explicitly marked as suspended
- **THEN** the aggregator flags the existing factors as `isSuspended: true` with their last known value, and immediately evicts any active surebets or value bets formed with those factors.

#### Scenario: Arbitrage evaluation ignoring suspended factors
- **WHEN** the arbitrage matching engine evaluates opposing outcomes across bookmakers
- **THEN** factors flagged with `isSuspended: true` or exceeding the live staleness threshold are skipped, preventing the emission of phantom arbitrage alerts.
