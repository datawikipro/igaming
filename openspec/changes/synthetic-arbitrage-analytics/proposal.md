# Proposal: Synthetic Arbitrage & Predictive Fair-Odds Analytics

## Why

Traditional arbitrage betting (Surebets) requires two matching counter-legs across disparate bookmakers, where sum of inverse odds is $< 1.0$. Such opportunities suffer from short lifespans (seconds to minutes) and bookmaker account profiling.

By evaluating true team probabilities using historical data, recent form, H2H statistics, and sharp market de-marginalization (Pinnacle/Betfair), SmartBet can calculate and display a **Synthetic Arbitrage Rating (+X.X% Yield)** directly on the match page. This provides users with actionable, institutional-grade expected value (+EV) without requiring concurrent counter-bets.

## What Changes

- **Predictive Fair-Odds Engine**: Calculate true probability ($P_{\text{model}}$) and fair odds for favorites and underdogs using historical match data (`/api/v1/teams/{id}/past`), home pitch advantage weighting, and market consensus.
- **Synthetic Yield Computation**:
  $$\text{Yield}_{\text{synthetic}} = \left( K_{\text{bookmaker}}^{\text{max}} \times P_{\text{model}} \right) - 1$$
- **UI Match Hub Widget with "В разработке" (In Development) Status**:
  - Display favorite vs underdog assessment with calculated fair odds.
  - Highlight the highest bookmaker market price.
  - Present the potential synthetic yield percentage banner.
  - Clearly label the feature with a cyberpunk `⚡ В РАЗРАБОТКЕ` status badge to build anticipation.
- **Historical Odds Movement Collection (Line Movement & CLV)**:
  - Continuously track and store granular odds fluctuations (ticks) from opening to closing across all 52 bookmakers.
  - Capture Closing Line Value (CLV) from sharp books (Pinnacle) to benchmark soft bookmaker pricing inefficiencies.
- **Dual-Tier Database Architecture & T+1 Analytical Export**:
  - **Real-Time PostgreSQL (`igaming_aggregator`)**: High-throughput OLTP with RAM tablespaces (`/dev/shm/ramdata`) and `synchronous_commit = off`. Stores live odds and active matches without bloat.
  - **Analytical PostgreSQL (DWH / OLAP)**: Cold/analytical storage partitioned by month and sport for backtesting, model training, and long-term analytics.
  - **T+1 Day Export Pipeline**: Exactly 24 hours after match completion, an automated batch ETL job extracts full match metadata, final scores, and complete odds movement history into Analytical Postgres, then marks `match_record.is_exported = true`.
  - **Safe Purge**: The existing `AggregatorCleanupService` safely deletes finished matches with `is_exported = true` from the real-time database, ensuring zero data loss and optimal cluster performance.
- **Decoupled Architecture**: Zero runtime token costs (deterministic statistical modeling); prepared prompt contracts for optional offline batch AI calibration.

## Capabilities

### New Capabilities
- `synthetic-arbitrage`: Algorithmic favorite/outsider true odds estimation, synthetic arbitrage yield calculation, and UI presentation on match detail pages.
- `odds-history-dwh`: Recording granular line movements and automated T+1 export from real-time PostgreSQL to analytical PostgreSQL with `is_exported` flag lifecycle.

### Modified Capabilities
- `frontend-architecture`: Integrate the Synthetic Arbitrage widget into `MatchEnrichmentSection` with reactive state and responsive styling.
- `aggregator-core`: Add `odds_tick_log` capturing and integrate scheduled DWH exporter before `CleanupTaskService` purges old matches.

## Impact

- **Frontend**: `MatchEnrichmentSection.tsx` and `matches-detail.css` on `smartbet.guru`.
- **Backend / Aggregator**: Historical form integration, `odds_tick_log` collection, and T+1 day DWH export job.
- **Database Infrastructure**: Clean separation of hot real-time Postgres and partitioned cold analytical Postgres.
- **Zero LLM Token Cost**: Real-time evaluation runs mathematically without calling external LLM APIs.
