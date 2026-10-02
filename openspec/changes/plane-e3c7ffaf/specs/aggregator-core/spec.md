# Aggregator Core Specification Delta (plane-e3c7ffaf)

## Purpose
Specifies Prometheus metrics telemetry and internal yield dashboard endpoints for monitoring surebet performance and coverage growth following market expansion in aggregator-surebet.

## ADDED Requirements

### Requirement: Surebet Prometheus Metrics Telemetry
The arbitrage engine in `aggregator-surebet` must collect and expose Micrometer metrics accessible via the Prometheus actuator endpoint `/actuator/prometheus`.

#### Scenario: Incrementing generated surebet counter
- **WHEN** a valid arbitrage situation is detected and passed through validation
- **THEN** `SurebetMetricsService` increments the counter `surebet_generated_total` with tags `sport` and `market_type`.

#### Scenario: Sampling yield distribution
- **WHEN** an arbitrage situation with profit percentage \( P \) is registered
- **THEN** the profit percentage is recorded into `surebet_yield_distribution` distribution summary across SLA/SLO yield buckets (0-1%, 1-3%, 3-5%, 5-10%, 10%+).

#### Scenario: Tracking active surebets by market type
- **WHEN** the scan iteration completes and active alerts are evaluated
- **THEN** the gauge `surebet_active_gauge` reflects the current number of active arbitrage alerts grouped by `market_type`.

#### Scenario: Recording cross-bookmaker arbitrage matrix
- **WHEN** a surebet involving two bookmakers \( BM_A \) and \( BM_B \) is detected
- **THEN** the counter `surebet_cross_bookmaker_matrix` is incremented with tags `bookmaker_a`, `bookmaker_b`, and `market_type`.

---

### Requirement: Surebet Yield & Market Dashboard API
The service must expose an internal REST API providing structured aggregation of surebet yield distributions, market breakdowns, and verification of market expansion uplift.

#### Scenario: Querying yield distribution and market statistics dashboard
- **WHEN** an HTTP `GET` request is made to `/api/v1/surebets/dashboard`
- **THEN** the response returns HTTP 200 with JSON payload containing total surebets, yield tier buckets, active counts per market type, cross-bookmaker matrix rankings, and confirmed uplift percentage.
