# Crawler Engine Specification Delta: Ingestion Pipeline Dashboard & Thresholds Monitor

## ADDED Requirements

### Requirement: Ingestion Pipeline Health and Throughput Monitoring
The crawler engine and aggregation subsystem must expose real-time metrics summarizing active bookmaker crawler states, message throughput on Kafka topic `odds.updates`, PostgreSQL odds ingestion persistence, and overall pipeline operational health.

#### Scenario: Ingestion pipeline statistics aggregation
- **WHEN** the operator accesses the Ingestion Pipeline statistics endpoint (`/api/v1/crawler-ops/pipeline/stats`)
- **THEN** the system aggregates total active matches, teams, unmapped odds, crawler delays, and pipeline throughput, returning an aggregated status of `HEALTHY`, `DEGRADED`, or `DOWN`.

---

### Requirement: Golden Rule #8 Thresholds Monitor (>= 500 Matches Standard)
The crawler operations console must continuously monitor each configured bookmaker scraper against the platform standard of at least 500 active matches, classifying each bookmaker as `COMPLIANT` (>= 500 matches), `DEGRADED` (< 500 matches or delay > 5 minutes), or `DEFECT` (0 matches or offline).

#### Scenario: Real-time threshold evaluation
- **WHEN** the thresholds endpoint (`/api/v1/crawler-ops/thresholds`) or dashboard is requested
- **THEN** each bookmaker's active match count is evaluated against the 500-match threshold, reporting the numerical progress percentage, compliance badge, proxy route, and sport distribution.

#### Scenario: Operational fleet force refresh
- **WHEN** an operator triggers a force refresh via `POST /api/v1/crawler-ops/fleet/refresh`
- **THEN** the aggregator invalidates cached delay and match count projections, re-evaluates all 52+ bookmakers, and returns updated threshold metrics immediately.
