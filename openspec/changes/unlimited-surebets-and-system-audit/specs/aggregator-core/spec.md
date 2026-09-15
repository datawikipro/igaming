## MODIFIED Requirements

### Requirement: In-Memory Surebet Detection
The arbitrage engine must evaluate opposing betting outcomes across bookmakers in memory with sub-second latency and compute guaranteed return margins without artificial yield ceilings. All mathematically valid arbitrage opportunities must immediately enter the `ACTIVE` state to be presented to consumers.

#### Scenario: Detecting two-way arbitrage (Surebet) without yield caps
- **WHEN** reciprocal decimal odds satisfy \( \frac{1}{O_1} + \frac{1}{O_2} < 1.0 \) for opposing outcomes of the same event across different bookmakers
- **THEN** a `Surebet` entity is instantiated, assigned status `ACTIVE` regardless of whether profit exceeds 10%, 50%, or higher, stored in PostgreSQL, and broadcast to subscribed consumers via Redis / WebSocket.

#### Scenario: Detecting three-way arbitrage without yield caps
- **WHEN** three-way 1X2 outcomes across bookmakers satisfy \( \frac{1}{O_1} + \frac{1}{O_X} + \frac{1}{O_2} < 1.0 \)
- **THEN** a 3-way `Surebet` is computed with calculated optimal stake distributions and published immediately as `ACTIVE`.

### Requirement: Odds Anomaly Tracking and Resolution
The aggregator must maintain a centralized registry of odds anomalies (`odds_anomaly`) ingested from crawlers and surebet evaluations, supporting deduplication, raw payload retrieval, and resolution workflows, while never suppressing or delaying active consumer alerts.

#### Scenario: Aggregating recurring anomalies
- **WHEN** an anomaly for the same bookmaker, event ID, and anomaly type is received within 1 hour
- **THEN** the existing pending anomaly's `occurrence_count` is incremented and `last_detected_at` is updated without duplicating storage.

#### Scenario: High-profit surebet anomaly telemetry without alert suppression
- **WHEN** an evaluated surebet profit exceeds normal historical statistical distributions
- **THEN** an `EXTREME_SUREBET` anomaly is asynchronously registered in `odds_anomaly` for engineer/AI review, high-priority odds refresh is scheduled, but the alert status SHALL remain `ACTIVE` and visible to users.
