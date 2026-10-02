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

### Requirement: Surebet Prometheus Metrics Telemetry
The arbitrage engine in `aggregator-surebet` must collect and expose Micrometer metrics accessible via the Prometheus actuator endpoint `/actuator/prometheus`.

#### Scenario: Incrementing generated surebet counter
- **WHEN** a valid arbitrage situation is detected and passed through validation
- **THEN** `SurebetMetricsService` increments the counter `surebet_generated_total` with tags `sport` and `market_type`.

#### Scenario: Sampling yield distribution
- **WHEN** an arbitrage situation with profit percentage \( P \) is registered
- **THEN** the profit percentage is recorded into `surebet_yield_distribution` distribution summary across SLA/SLO yield buckets (0-1%, 1-3%, 3-5%, 5-10%, 10%+).

#### Scenario: Tracking active surebets by market type
- **WHEN** the scan iteration completes and active alerts are evaluated
- **THEN** the gauge `surebet_active_gauge` reflects the current number of active arbitrage alerts grouped by `market_type`.

#### Scenario: Recording cross-bookmaker arbitrage matrix
- **WHEN** a surebet involving two bookmakers \( BM_A \) and \( BM_B \) is detected
- **THEN** the counter `surebet_cross_bookmaker_matrix` is incremented with tags `bookmaker_a`, `bookmaker_b`, and `market_type`.

---

### Requirement: Surebet Yield & Market Dashboard API
The service must expose an internal REST API providing structured aggregation of surebet yield distributions, market breakdowns, and verification of market expansion uplift.

#### Scenario: Querying yield distribution and market statistics dashboard
- **WHEN** an HTTP `GET` request is made to `/api/v1/surebets/dashboard`
- **THEN** the response returns HTTP 200 with JSON payload containing total surebets, yield tier buckets, active counts per market type, cross-bookmaker matrix rankings, and confirmed uplift percentage.

