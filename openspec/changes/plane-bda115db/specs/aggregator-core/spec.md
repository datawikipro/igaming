# Aggregator Core Specification Delta (plane-bda115db)

## Purpose
Specifies structural anomaly detection, inverted outcome validation, No Silent Drop telemetry audit, and No Yield Cap enforcement within the aggregator arbitrage evaluation pipeline.

## ADDED Requirements

### Requirement: Structural Anomaly and Monotonicity Detection
The evaluation pipeline in `SurebetRuleEvaluator` must detect internal line distortions within a single bookmaker feed, including negative margins and inverted parameter progressions for totals and handicaps, and discard corrupt candidate odds.

#### Scenario: Detecting single-bookmaker negative margin
- **WHEN** a single bookmaker publishes opposing outcomes for a market where \( \sum \frac{1}{O_i} < 0.99 \)
- **THEN** the bookmaker's corrupted odds for that market group are discarded and a `NEGATIVE_MARGIN` anomaly of severity `CRITICAL` is recorded in `OddsAnomalyService`.

#### Scenario: Detecting inverted total progression
- **WHEN** a bookmaker's odds for totals satisfy \( Over(T_2) < Over(T_1) - 0.20 \) or \( Under(T_1) < Under(T_2) - 0.20 \) for parameters \( T_2 > T_1 \)
- **THEN** the bookmaker's odds for that market are discarded and an `INVERTED_TOTAL_PROGRESSION` anomaly of severity `ERROR` is recorded in `OddsAnomalyService`.

#### Scenario: Detecting inverted handicap progression
- **WHEN** a bookmaker's odds for handicaps satisfy \( H_1(H_2) > H_1(H_1) + 0.20 \) or \( H_2(H_2) < H_2(H_1) - 0.20 \) for normalized parameters \( H_2 > H_1 \)
- **THEN** the bookmaker's odds for that market are discarded and an `INVERTED_HANDICAP` anomaly of severity `ERROR` is recorded in `OddsAnomalyService`.

---

### Requirement: Inverted 3-Way Outcome Validation
The validation pipeline must detect and reject inverted 3-way (1X2) outcome triads where both teams are presented as deep underdogs or with mutually exclusive probability bounds.

#### Scenario: Detecting dual-underdog 3-way inversion
- **WHEN** a candidate 3-way combination exhibits \( K(WIN1) > 3.20 \) and \( K(WIN2) > 3.20 \), or \( K(WIN1) > 3.0 \) and \( K(WIN2) > 3.0 \) with \( K(DRAW) > 2.80 \)
- **THEN** `InvertedOutcomeRule` rejects the combination, prevents alert generation, and logs `INVERTED_OUTCOME_DOMINANCE_VIOLATION`.

---

### Requirement: No Silent Drop Audit Telemetry
Every rejection by any validation rule in `SurebetValidator` must be persisted to `OddsAnomalyService` with complete match metadata, participants, severity, and diagnostic payload.

#### Scenario: Recording rejected candidate telemetry
- **WHEN** a candidate arbitrage combination fails validation by any registered `SurebetValidationRule`
- **THEN** `SurebetValidator` constructs an `OddsAnomalyDto` with severity `ERROR` and forwards it to `OddsAnomalyService.recordAnomaly()`.

---

### Requirement: No Yield Cap Preservation
Arbitrage situations with mathematically valid returns must not be dropped or artificially capped.

#### Scenario: Processing high-yield surebets
- **WHEN** a valid arbitrage situation exceeds the anomaly telemetry threshold (>30% live or >50% prematch)
- **THEN** `SurebetRuleEvaluator` records an `EXTREME_SUREBET` warning telemetry event and schedules high-priority refresh, while preserving the surebet with status `ACTIVE`.
