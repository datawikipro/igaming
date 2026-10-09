# Tasks: Synthetic Arbitrage & Predictive Fair-Odds Analytics

## Checklist

- [x] 1. Architectural RFC & Specification
  - [x] Formulate mathematical definition of synthetic arbitrage yield.
  - [x] Document data pipeline (H2H, team form, de-marginalization).
  - [x] Save RFC in `smartbet.guru/docs/SYNTHETIC_ARBITRAGE_ANALYTICS_SPEC.md`.

- [x] 2. OpenSpec Change Proposal
  - [x] Create `openspec/changes/synthetic-arbitrage-analytics/proposal.md`.
  - [x] Create `openspec/changes/synthetic-arbitrage-analytics/design.md`.
  - [x] Create `openspec/changes/synthetic-arbitrage-analytics/tasks.md`.
  - [x] Create `openspec/changes/synthetic-arbitrage-analytics/specs/synthetic-arbitrage/spec.md`.

- [x] 3. UI Implementation on Match Page (`smartbet.guru`)
  - [x] Integrate Synthetic Arbitrage glass card in `MatchEnrichmentSection.tsx`.
  - [x] Render Favorite vs Outsider evaluation with fair odds.
  - [x] Display prominent `⚡ В РАЗРАБОТКЕ` (In Development) status badge.
  - [x] Compute dynamic synthetic yield percentage based on live market odds.
  - [x] Display DWH tick retention and line movement indicator.

- [ ] 4. Historical Odds Movement & DWH Pipeline (`igaming-aggregator` / `igaming_analytics`)
  - [ ] Create `odds_tick_log` table in real-time PostgreSQL database.
  - [ ] Wire tick capture on crawler price updates (`OddsUpsertManager`).
  - [ ] Implement `DwhBatchExporterService` scheduled cron (T+1 d after match start).
  - [ ] Create partitioned analytical tables (`dwh_dim_match`, `dwh_fact_odds_movement`, `dwh_agg_closing_odds`).
  - [ ] Set `is_exported = true` on `match_record` upon successful export.
  - [ ] Verify `CleanupTaskService.deleteOldMatchesAggressive` safely retains unexported matches.

- [ ] 5. Verification & Dev-Deploy
  - [ ] Build and deploy container on Xeon host (`100.78.183.101:30500/smartbet.guru:latest`).
  - [ ] Rollout restart in `igaming-dev` namespace.
  - [ ] Verify 200 OK status on match pages with active widget.
