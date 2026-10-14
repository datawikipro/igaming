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
The arbitrage engine MUST evaluate opposing betting outcomes across bookmakers in memory with sub-second latency, pre-fetching candidate match metadata in bulk batches and dispatching parallel evaluation tasks across Java 21 Virtual Threads to eliminate N+1 database round-trips.

#### Scenario: Detecting two-way arbitrage (Surebet)
- **WHEN** reciprocal decimal odds satisfy \( \frac{1}{O_1} + \frac{1}{O_2} < 1.0 \) for opposing outcomes of the same event
- **THEN** a `Surebet` entity is instantiated, stored in PostgreSQL, and broadcast to subscribed consumers via Redis / WebSocket.

#### Scenario: Detecting three-way arbitrage
- **WHEN** three-way 1X2 outcomes across bookmakers satisfy \( \frac{1}{O_1} + \frac{1}{O_X} + \frac{1}{O_2} < 1.0 \)
- **THEN** a 3-way `Surebet` is computed with calculated optimal stake distributions.

#### Scenario: Batch metadata pre-fetching during full scan
- **WHEN** a full or incremental surebet scan cycle executes across candidate match IDs
- **THEN** event metadata is pre-fetched in bulk via a single batch query (`findMetadataByIdIn`) and evaluated concurrently across Virtual Threads without opening individual database transactions per match.

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

### Requirement: High-Throughput Batch Odds Persistence
The aggregator ingestion engine MUST persist normalized odds for any event in atomic batches to both PostgreSQL and Redis, avoiding sequential single-row inserts and commands.

#### Scenario: Batch persisting odds for a match
- **WHEN** an odds update with multiple outcomes is processed for an event
- **THEN** all normalized odds are persisted to PostgreSQL in a single batch operation and written to Redis via pipelined commands in one network round-trip.

#### Scenario: Deterministic deadlock prevention
- **WHEN** concurrent odds updates for multiple events or the same event arrive from different bookmakers
- **THEN** odds records are sorted deterministically before persistence, preventing database transaction deadlocks (`40P01`).

#### Scenario: Fallback on partial batch error
- **WHEN** a batch upsert encounters a database constraint or serialization failure
- **THEN** the system falls back to resilient individual upserts without dropping valid quotes or triggering unhandled exceptions.

