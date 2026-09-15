## ADDED Requirements

### Requirement: Bookmaker Fleet Operational Visibility
The aggregator core SHALL maintain a real-time operational status for all 52+ supported bookmakers, tracking match volume against Golden Rule #8 (>= 500 matches threshold), total odds, delay in minutes, sport distribution, and top leagues.

#### Scenario: Fleet overview query
- **WHEN** an administrator requests `GET /api/bookmakers/fleet`
- **THEN** the system returns a `FleetOverviewDto` containing status classification (`HEALTHY`, `DEGRADED`, `OFFLINE`), active match counts, and Golden Rule #8 compliance metrics.

#### Scenario: Instant cache invalidation
- **WHEN** an administrator requests `POST /api/bookmakers/fleet/refresh`
- **THEN** the Redis cache key `unify:bookmaker_fleet_status` is evicted and refreshed immediately from the latest repository projections.