# Proposal: Esports Canonical MDM Hierarchy, Discipline Reclassification & Autonomous Settlement

## Executive Summary
This proposal establishes an institutional 4-tier domain hierarchy for competitive esports within the MDM subsystem, resolves the legacy bucket dilemma where 16,353 matches were collapsed into generic `sport_id = 6` (`esports`), introduces mathematical match-level `best_of` market validation, and wires live score fallback from `esports_raw_events` (HLTV & OpenDota) directly into `MatchSettlementService`.

## Problem Statement
1. **Generic Bucket Collapse**: 16,353 competitive matches across disparate games (CS2, Dota 2, League of Legends, Cyber Football, etc.) were lumped into a single `sport_id = 6` (`esports`), preventing discipline-specific statistical models, vector spaces, and rulebook settlements.
2. **Missing Stage Hierarchy**: Tournaments lacked structured stages (Group stage, Swiss rounds, Upper/Lower brackets, Playoffs), hindering contextual analysis of team motivation and bracket pressure.
3. **Format Conflation**: Match series format (`best_of = 1, 3, 5, 7`) was incorrectly treated at tournament level rather than match level, producing impossible betting markets (e.g. Map Totals `TOTAL_OVER_2.5` offered on Bo1 matches).
4. **Settlement Voiding**: Lack of real-time ground truth score propagation caused AI recommendations on esports matches to expire into `VOID (-:-)` after normal duration timeouts.

## Proposed Solution
1. **4-Level Hierarchy Architecture**:
   - `mdm_tournament` (Лига/Турнир) $\rightarrow$ `mdm_season` (Сезон) $\rightarrow$ `mdm_season_stage` (Стадия Сезона) $\rightarrow$ `mdm_match` (Матч).
   - Polymorphic bracket formats: `TournamentBracketFormat` (`SWISS`, `DOUBLE_ELIMINATION`, `SINGLE_ELIMINATION`, `GSL_GROUPS`, `ROUND_ROBIN`).
2. **Discipline Reclassification**:
   - Split generic `sport_id = 6` into canonical IDs:
     - `33`: CS2 (`cs2`)
     - `34`: Dota 2 (`dota2`)
     - `35`: League of Legends (`league-of-legends`)
     - `36`: Valorant (`valorant`)
     - `37`: Cyber Football / FIFA (`cyber-football`)
     - `38`: Rainbow Six (`rainbow-six`)
     - `39`: Starcraft II (`starcraft`)
     - `40`: Mobile Legends (`mobile-legends`)
     - `41`: Cyber Basketball (`cyber-basketball`)
     - `42`: Cyber Hockey (`cyber-hockey`)
3. **Match-Level `best_of` Validation & Universal Formulas**:
   - For series of $B$ games: required wins $W = (B + 1) / 2$.
   - Valid map total thresholds: $K - 0.5$ for $K \in [W + 1, B]$.
   - Valid map handicap thresholds: $\pm(H - 0.5)$ for $H \in [1, W - 1]$.
   - Bo1 ($B = 1$): strictly prohibits map totals and map handicaps; allows only match winner (`WIN1/WIN2`) and single-map round lines.
4. **Ground Truth Score Ingestion & Settlement Fallback**:
   - Continuous ingestion of CS2 (HLTV) and Dota 2 (OpenDota) pro matches via `igaming-capture-esports` into `esports_raw_events`.
   - Direct FDW integration into `igaming_analytics` allowing `MatchSettlementService` to resolve confirmed scores prior to expiration timeouts.
