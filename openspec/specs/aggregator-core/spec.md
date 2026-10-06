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

#### Scenario: Handling extreme yield surebets under No Yield Cap policy
- **WHEN** a valid cross-bookmaker surebet yields an extreme profit percentage exceeding telemetry thresholds
- **THEN** the surebet is published with full yield without artificial yield clipping (No Yield Cap), and an `EXTREME_SUREBET` warning is recorded in `odds_anomaly` telemetry triggering a high-priority odds refresh.

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

### Requirement: Syndicate Clone and Temporal Validation
The arbitrage engine must validate candidate surebet pairings against syndicate clone rules and temporal live/prematch separation to eliminate phantom super-arbitrage alerts.

#### Scenario: Rejecting clone syndicate pairings
- **WHEN** opposing odds originate from bookmakers within the same feed syndicate or clone family (such as Betcity family: betcity and betcity-com; or BetB2B clones: FanSport, 1xBet, Melbet, Megapari, Linebet, BetAndYou, 888starz, SpinBetter)
- **THEN** `CloneSyndicateRule` invalidates the pair and suppresses alert generation.

#### Scenario: Rejecting temporal live-prematch mismatch
- **WHEN** candidate outcomes span mismatched Live and Prematch event states
- **THEN** `LivePrematchSeparationRule` discards the pairing.


