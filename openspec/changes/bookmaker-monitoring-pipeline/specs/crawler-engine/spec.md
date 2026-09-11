## ADDED Requirements

### Requirement: Staggered Node Throttling and Health Heartbeats
Bookmaker crawler and loader pods SHALL perform staggered startup delays and periodically send heartbeats to the portal and aggregator gateways to register active status and supported regions.

#### Scenario: Staggered startup under CPU load
- **WHEN** a crawler or loader pod initializes on a shared Kubernetes worker node
- **THEN** it checks local node CPU utilization and delays startup until load is below safe execution thresholds.

#### Scenario: Heartbeat transmission
- **WHEN** a bookmaker loader is running in profile `match-loader`
- **THEN** it sends periodic heartbeat requests containing bookmaker ID, display name, and active regions to `igaming-portal` and `igaming-aggregator`.
