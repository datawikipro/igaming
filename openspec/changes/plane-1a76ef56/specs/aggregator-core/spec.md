# Aggregator Core Specification Delta (plane-1a76ef56)

## Purpose
Specifies arbitrage validation rules preventing anomalous surebet alerts involving Winline handicap and total market permutations.

## ADDED Requirements

### Requirement: Anomalous Surebet Elimination for 3-Way and Polarity Discrepancies
The arbitrage evaluation engine must ensure that any incoming odds from Winline conform to strict 2-way binary/asian models before evaluating candidate surebets exceeding the normal threshold.

#### Scenario: Suppressing false surebets caused by 3-way markets
- **WHEN** odds from Winline are ingested
- **THEN** only pure 2-way outcomes are processed by the arbitrage engine, preventing artificial surebets exceeding normal market expectations.
