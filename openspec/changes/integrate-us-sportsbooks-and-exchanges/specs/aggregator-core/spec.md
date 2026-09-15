## ADDED Requirements

### Requirement: Exchange Orderbook Arbitrage Matching
The arbitrage engine SHALL evaluate Surebets and +EV opportunities formed between conventional bookmakers and peer-to-peer betting exchanges, taking into account net exchange commission rates.

#### Scenario: Matching bookmaker odds against exchange lay liquidity
- **WHEN** a conventional bookmaker offers back odds \( O_{back} \) and an exchange offers lay odds \( O_{lay} \) with sufficient liquidity
- **THEN** an arbitrage position is computed with net margin adjusted for exchange commission.

---

### Requirement: Player Props Multi-Dimensional Matching
The aggregator SHALL match and compare player prop totals across sportsbooks and DFS platforms using canonical athlete identity mapping.

#### Scenario: Detecting player prop line discrepancies
- **WHEN** two bookmakers or DFS platforms publish differing lines for the same athlete and statistic (e.g. Points Over 24.5 vs Under 26.5)
- **THEN** the system generates a cross-bookmaker middle or arbitrage alert for the player prop.
