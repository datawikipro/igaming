# Capability: Synthetic Arbitrage & Predictive Fair-Odds Analytics

## Requirements

### Requirement: Synthetic Arbitrage Evaluation
The system MUST calculate expected value and synthetic yield by comparing bookmaker live odds against true fair odds derived from historical match data and market consensus.

#### Scenario: Mathematical Yield Calculation
- Given a match with favorite implied probability $P_{\text{favorite}}$ and maximum bookmaker odds $K_{\text{max}}$
- When evaluating synthetic arbitrage
- Then the yield MUST be computed as $(K_{\text{max}} \times P_{\text{favorite}}) - 1$
- And displayed as a percentage gain over the market.

### Requirement: UI Match Detail Presentation
The match detail page MUST provide a dedicated visual component presenting the synthetic arbitrage assessment.

#### Scenario: Displaying Status Badge
- When rendering the synthetic arbitrage card on `/matches/[slug]`
- Then it MUST display an explicit `⚡ В РАЗРАБОТКЕ` badge
- And present the favorite and underdog fair odds comparison.

### Requirement: Perpetual Historical Odds Collection (Line Movement)
The system MUST record granular price fluctuations (ticks) from opening to closing across all integrated bookmakers to enable CLV analysis and predictive calibration.

#### Scenario: Tick Ingestion
- Given an incoming odds update from a bookmaker crawler
- When the price deviates from the currently stored line
- Then a new record MUST be appended to `odds_tick_log` with timestamp, match ID, bookmaker, and value.

### Requirement: Dual-Tier Storage & T+1 Analytical Export
The system MUST export completed matches and their entire odds tick history to the analytical PostgreSQL DWH 24 hours after match completion, preserving operational database performance.

#### Scenario: Export and Cleanup Lifecycle
- Given a match completed more than 24 hours ago with `is_exported = false`
- When the scheduled batch exporter runs
- Then match metadata and all corresponding `odds_tick_log` entries MUST be transferred to `dwh_dim_match` and `dwh_fact_odds_movement`
- And `match_record.is_exported` MUST be set to `true`
- And the operational real-time cleanup job MUST only delete matches with `is_exported = true`.
