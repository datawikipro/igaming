# Aggregator Core Specification

## Purpose
Collects, normalizes, and matches real-time betting odds across bookmakers to detect arbitrage opportunities (Surebets), value bets (+EV), and middle bets in memory with high-throughput PostgreSQL persistence.

## Requirements

### Requirement: Kafka Ingestion and Normalization
The ingestion pipeline must consume raw odds updates from Kafka `odds.updates`, normalize team names, player names, and tournaments using the sport normalization dictionary, and deduplicate updates.

#### Scenario: Ingesting bookmaker event odds
- **WHEN** a valid odds update message is consumed from Kafka
- **THEN** team names and league names are normalized against canonical dictionary identifiers and passed to the arbitrage matching engine.

---

### Requirement: In-Memory Surebet Detection
The arbitrage engine must evaluate opposing betting outcomes across bookmakers in memory with sub-second latency and compute guaranteed return margins.

#### Scenario: Detecting two-way arbitrage (Surebet)
- **WHEN** reciprocal decimal odds satisfy \( \frac{1}{O_1} + \frac{1}{O_2} < 1.0 \) for opposing outcomes of the same event
- **THEN** a `Surebet` entity is instantiated, stored in PostgreSQL, and broadcast to subscribed consumers via Redis / WebSocket.

#### Scenario: Detecting three-way arbitrage
- **WHEN** three-way 1X2 outcomes across bookmakers satisfy \( \frac{1}{O_1} + \frac{1}{O_X} + \frac{1}{O_2} < 1.0 \)
- **THEN** a 3-way `Surebet` is computed with calculated optimal stake distributions.

---

### Requirement: Value Bets and Middles Calculation
The system must calculate mathematically positive expectation (+EV / ValueBets) against pinnacle/sharp benchmarks and identify overlapping handicap/total intervals (Middles).

#### Scenario: Value bet identification
- **WHEN** a soft bookmaker's odds significantly exceed the de-margined true probability derived from sharp benchmark bookmakers
- **THEN** a `ValueBet` is published with positive expected value percentage.

#### Scenario: Middle bet identification
- **WHEN** opposing spread or total bets across two bookmakers create a winning intersection range
- **THEN** a `Middle` event is generated with probability of double win and max loss risk metrics.

---

### Requirement: Odds Anomaly Tracking and Resolution
The aggregator must maintain a centralized registry of odds anomalies (`odds_anomaly`) ingested from crawlers and surebet evaluations, supporting deduplication, raw payload retrieval, and resolution workflows.

#### Scenario: Aggregating recurring anomalies
- **WHEN** an anomaly for the same bookmaker, event ID, and anomaly type is received within 1 hour
- **THEN** the existing pending anomaly's `occurrence_count` is incremented and `last_detected_at` is updated without duplicating storage.

#### Scenario: High-profit surebet anomaly flagging
- **WHEN** an evaluated surebet profit exceeds the maximum allowable threshold (> 50%)
- **THEN** an `EXTREME_SUREBET` anomaly is registered, targeted refresh is scheduled, and the alert status is set to `PENDING_VERIFICATION`.
