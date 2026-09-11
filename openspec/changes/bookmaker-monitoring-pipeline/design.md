## Context

The SmartBet.guru ecosystem comprises 36+ bookmaker crawler and loader modules in Kubernetes (`igaming-source`), a centralized Kafka broker (`kafka.igaming-master`), and an aggregation core (`igaming-aggregator-ingestion`, `igaming-aggregator-surebet`, `igaming-aggregator-api`) backed by PostgreSQL and Redis.

## Goals / Non-Goals

**Goals:**
- Maintain active scraping and ingestion across primary Russian & international bookmakers (`winline`, `fon-bet-ru`, `leon`, `marathonbet`, `olimpbet`, `betcity`, `baltbet`, `zenit`, `pinnacle`).
- Eliminate PostgreSQL native upsert constraint errors in `odds_actual` through the expression-based unique index `uq_odds_actual_upsert`.
- Ensure continuous scheduled scanning in `igaming-aggregator-surebet` for arbitrage opportunities (Surebets) and +EV value bets.
- Deploy background subagents and periodic monitoring tasks for real-time telemetry on Kafka lags and loader heartbeats.

**Non-Goals:**
- Refactoring the frontend Next.js application (managed separately under `WebstormProjects/igaming`).
- Implementing new payment gateways or user subscription billing workflows.

## Decisions

### Decision 1: Expression-based Unique Index on `odds_actual`
- **Choice**: Add `CREATE UNIQUE INDEX IF NOT EXISTS uq_odds_actual_upsert ON odds_actual (match_id, bet_source_id, odds_type_id, COALESCE(param, -10000.0), COALESCE(outright_selection_id, -1));`
- **Rationale**: PostgreSQL `ON CONFLICT (match_id, bet_source_id, odds_type_id, COALESCE(param, -10000.0), COALESCE(outright_selection_id, -1))` requires a matching unique index or constraint. Without it, Hibernate native queries throw `PSQLException`.
- **Alternatives**: Using application-level locking (severe performance bottleneck under Kafka stream load).

### Decision 2: Staggered Node Startup with `wait-for-low-cpu`
- **Choice**: Utilize init containers with cksum-based hostname delay and CPU threshold checks (<90%).
- **Rationale**: Prevents CPU throttling and node starvation when multiple Spring Boot JVMs start concurrently.

### Decision 3: Subagent & Background Process Monitoring
- **Choice**: Run autonomous monitoring tasks using subagents and background schedulers to track Kafka message volume, active match counts, and surebet occurrences.

## Risks / Trade-offs

- **[Risk: DB connection pool exhaustion]** → Fixed via HikariCP non-blocking initialization timeout and setting `maximum-pool-size` per role.
- **[Risk: Bookmaker anti-bot rate limits]** → Mitigated via rotating residential/datacenter proxy pool through `service-proxy-backend`.
