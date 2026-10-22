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

### Requirement: Virtual Threads Async Dispatching
The arbitrage engine MUST execute asynchronous event evaluation tasks (`@Async("surebetExecutor")`) on Java 21 virtual threads to guarantee non-blocking task execution and prevent thread pool starvation.

#### Scenario: Asynchronous surebet detection execution
- **WHEN** `SurebetDetectorService.detectSurebetsForMatch` is invoked asynchronously
- **THEN** it SHALL be executed on a dedicated Java 21 virtual thread managed by `surebetExecutor`.

---

### Requirement: Parallel Odds Normalization Processing
The entity normalization pipeline MUST execute normalization requests and odds type resolution concurrently on Java 21 virtual threads, preventing scheduler stalling and platform thread exhaustion during remote LLM Gateway calls.

#### Scenario: Parallel odds type batch processing
- **WHEN** a batch of pending `ODDS_TYPE` normalization requests is polled
- **THEN** requests SHALL be processed concurrently in parallel tasks, allowing fast-track in-memory matches to complete without waiting on remote LLM evaluations.

#### Scenario: Virtual thread execution for LLM normalization
- **WHEN** normalization requests require remote LLM Gateway evaluation
- **THEN** execution SHALL be handled by a virtual threads executor without fixed platform thread pool size constraints.

---

### Requirement: FastTrack In-Memory Odds Resolution
The odds normalization and ingestion pipeline MUST resolve common market outcomes, match results (1X2), double chances, both teams to score (BTTS), and standard totals in-memory via `FastTrackDictionary` with $O(1)$ complexity prior to database fallback, preventing temporary database record churn and unnecessary LLM gateway invocations.

#### Scenario: In-memory resolution of standard betting outcomes
- **WHEN** an odds update with common outcomes (e.g., 1X, X2, 12, BTTS, Over/Under, Win1/Win2) is received without a pre-existing alias
- **THEN** `FastTrackDictionary` SHALL resolve the raw outcome to its canonical `BetType` in memory without triggering background LLM normalization requests or generating temporary `TEMP_` odds types.

#### Scenario: Sub-millisecond dictionary lookup performance
- **WHEN** sports or odds names are resolved
- **THEN** lookups SHALL execute in $O(1)$ time complexity using pre-indexed case-insensitive lookup tables and pre-compiled regex patterns.

---

### Requirement: Bi-Temporal Historical Match Ledger and Sliding-Window Team DNA
The aggregator engine MUST persist every finished match in a dedicated chronological ledger (`team_match_history`) ordered strictly by `match_date DESC`, supporting out-of-order historical backfilling and deterministic, pure sliding-window Team DNA tag evaluation (`TeamDnaProfile`).

#### Scenario: Chronological match history recording
- **WHEN** a finished match result or historical encounter is ingested
- **THEN** it SHALL be recorded in `team_match_history` with unique constraint `(sport, team_name, source_event_id)` and indexed on `(sport, team_name, match_date DESC)`.

#### Scenario: Automatic past match backfilling from H2H payload
- **WHEN** an enriched raw event containing H2H previous encounters is received
- **THEN** past matches are extracted and backfilled into `team_match_history` without requiring supplementary HTTP requests.

#### Scenario: Out-of-order backfill consistency and DNA re-evaluation
- **WHEN** older historical matches arrive out of order (e.g., February encounters backfilled after April)
- **THEN** the sliding-window query `ORDER BY match_date DESC LIMIT 15` automatically places them in exact chronological sequence, preventing data drift and zero-lookahead corruption.

#### Scenario: Reactive narrative regeneration on DNA shift
- **WHEN** the newly evaluated DNA tags differ from the existing profile (`TeamDnaProfile.dna_tags`)
- **THEN** the system SHALL update the DNA profile and proactively regenerate pre-match storyboard narratives for all upcoming matches involving that team.

---

### Requirement: Reactive Living Storyboard Preview and Multilingual Synthesis
The aggregator storyboard engine MUST generate rich, multi-section pre-match previews and narratives (`MatchCombinatorialNarrativeDto`) on demand and reactively via polymorphic rule-based strategies across 5 languages (RU, EN, FR, DE, ES) without incurring external LLM latency or cost.

#### Scenario: On-demand match storyboard initialization
- **WHEN** a client or ingestion service invokes `POST /api/matches/{id}/storyboard/init`
- **THEN** the engine loads the match, current team DNA profiles, and context tags, compiles the narrative sections (`introSection`, `tacticalDeepDiveSection`, `tradingPlanSection`), and stores the preview in `match_record.storyline_json`.

#### Scenario: Storyboard refresh on odds or lineup movement
- **WHEN** `POST /api/matches/{id}/storyboard/refresh` is triggered following significant line steam or confirmed lineups
- **THEN** the preview narrative is refreshed and synchronized with the latest tactical metrics.

---

### Requirement: Team Signature Normalization and Youth/Women Disambiguation
The entity normalization pipeline MUST parse gender (`Gender.FEMALE`), age category (`AgeCategory.U17..U23`, `YOUTH`), and team tier (`TeamTier.RESERVE`) from both team names and tournament context, strictly enforcing parity checks and multi-lingual transliteration in `SimilarityUtils` and `TeamSignatureNormalizer`.

#### Scenario: Disambiguating women and youth fixtures from senior squads
- **WHEN** team names contain gender indicators (e.g. `(W)`, `women`, `женщины`, `femmes`, `frauen`) or age categories (`U19`, `U21`, `молодёжные`, `юноши`) in event names or tournament descriptions
- **THEN** the system generates distinct canonical signatures (`«Челси (ж)»`, `«Спартак U19»`) and forbids matching across different gender or age categories, preventing zero-lookahead corruption and empty team cards.

#### Scenario: Cross-language team transliteration and context matching
- **WHEN** matching raw SofaScore or external bookmaker events against internal match records
- **THEN** `SimilarityUtils` strips diacritics, performs phonetic transliteration, and validates tournament context before applying Jaro-Winkler distance thresholds.

---

### Requirement: Odds Collision Incident Prevention and Multi-Line Deduplication
The odds normalization and source ingestion engine MUST detect and resolve factor collisions, overtime vs regular time scope ambiguities, and duplicate bookmaker line variations before persistence.

#### Scenario: Resolving overtime and regular time scope collision
- **WHEN** parsing market scopes containing regular time phrases alongside potential overtime keywords
- **THEN** `ScopeMatcher` strictly excludes regular time phrases from overtime classification, preventing invalid full match fallthrough and duplicate factor collisions.

#### Scenario: Tracking collision incidents
- **WHEN** conflicting odds factors for identical market groups are received from a bookmaker feed
- **THEN** `OddsCollisionIncidentService` logs and deduplicates line variations, maintaining line integrity without crashing the ingestion worker.

---

### Requirement: ClickHouse Time-Series Historical Odds & Redis Real-Time Active Line Storage
The aggregator engine MUST store active real-time match odds exclusively in Redis hashes and stream all historical price movements (ticks) asynchronously to ClickHouse (`igaming_history.odds_ticks`), completely eliminating PostgreSQL table writes for `odds_actual` and `odds_history` to prevent database locks and connection exhaustion.

#### Scenario: Real-time active odds upsert in Redis
- **WHEN** updated odds are received for a match
- **THEN** they SHALL be stored in Redis key `odds:match:{matchId}` with field format `{bookmaker}:{typeCode}:{param}`, supporting $O(1)$ lookups and multi-bookmaker aggregation without PostgreSQL table locks.

#### Scenario: Asynchronous batch streaming to ClickHouse
- **WHEN** new or changed odds ticks are processed by `OddsUpsertManager`
- **THEN** ticks SHALL be enqueued into `ClickHouseOddsHistoryService`'s in-memory non-blocking buffer and flushed in batches (up to 5,000 ticks/sec) via HTTP POST into `igaming_history.odds_ticks`.

#### Scenario: Reading odds time-series history
- **WHEN** a client or analyst requests historical odds movements via `/api/history/odds/by-match-and-types`
- **THEN** `HistoryOddsController` queries ClickHouse first for real-time high-resolution tick history, seamlessly falling back to legacy DB partitions only for dates preceding the ClickHouse migration.

---

### Requirement: Two-Tier Odds Verification, Quarantine Buffer & Reactive Sentry Refresher Protocol
The aggregator engine MUST protect active markets, surebet calculations, and ClickHouse time-series history from transient line inversions and bookmaker feed glitches (e.g. 3.00 -> 1.60 -> 3.00) using an automated two-tier verification and debounce mechanism:

#### Scenario: Detecting anomalous odds spike and entering quarantine
- **WHEN** an incoming odds value deviates from the previous value by $\ge 40\%$ ($|V_{new} - V_{prev}| / \min(V_{prev}, V_{new}) \ge 0.40$) or exhibits a favorite/underdog inversion
- **THEN** RedisOddsQuarantineService places the candidate quote into a 10-second quarantine buffer, retains the previous verified value in the active Redis hash, and withholds streaming the candidate tick to ClickHouse.

#### Scenario: Reactive refresher dispatch for 3-tier bookmakers
- **WHEN** an anomalous spike is quarantined for a 3-tier bookmaker (crawler, loader, refresher)
- **THEN** OddsRefreshService debounces duplicate bursts and pushes the externalEventId to Redis queue odds:refresh:{bookmaker}, awakening the dedicated reactive refresher sentry (GenericOddsRefreshScheduler) via BRPOP for an immediate single-event line re-fetch.

#### Scenario: Passive batch confirmation for full-feed bookmakers
- **WHEN** an anomalous spike is quarantined for a full-feed batch bookmaker without a dedicated refresher
- **THEN** the candidate quote remains quarantined awaiting confirmation from the subsequent scheduled full line batch.

#### Scenario: Confirming verified line movements
- **WHEN** the next line update from the bookmaker confirms the quarantined price within $\pm 5\%$ tolerance
- **THEN** RedisOddsQuarantineService clears the quarantine, commits the new verified quote to active Redis with isSuspended: false, and streams the tick to ClickHouseOddsHistoryService.

#### Scenario: Discarding transient glitches and inversions
- **WHEN** the next line update returns back towards the pre-spike price within $\pm 10\%$ tolerance
- **THEN** RedisOddsQuarantineService clears the quarantine, restores the verified line in Redis with isSuspended: false, and discards the transient glitch without polluting ClickHouse history or triggering false arbitrage alerts.

#### Scenario: 10-second timeout lock on unverified odds
- **WHEN** 10 seconds elapse with no verification response from the bookmaker or refresher sentry
- **THEN** RedisOddsQuarantineService.checkTimeouts locks the odd in Redis at its previous verified value with isSuspended: true, immediately evicting it from ggregator-surebet calculation and displaying a lock indicator on consumer interfaces until a fresh valid update arrives.
