## MODIFIED Requirements

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
