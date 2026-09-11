# Delta Spec: Crawler Engine Prematch Hardening and Resiliency

## Added Requirements

### Requirement: Bookmaker Prematch Line Ingestion
Each active bookmaker crawler SHALL ingest full prematch lines covering standard sports (Football, Hockey, Tennis, Basketball) in addition to live events.

#### Scenario: Zenit prematch line retrieval
- GIVEN a running `igaming-source-zenit-crawler`
- WHEN the prematch scheduler triggers
- THEN it SHALL fetch the full prematch printer catalog and persist discovered matches into `match_cache` with status `NEW`.

### Requirement: Proxy Exhaustion Graceful Fallback
Loaders and crawlers SHALL NOT terminate JVM on temporary proxy exhaustion.

#### Scenario: Proxy pool rotation backoff
- GIVEN proxy rotation reaches the maximum retry count
- WHEN `VpnManagerService` detects pool exhaustion
- THEN it SHALL apply exponential backoff (30s) and retry rather than executing `System.exit(1)`.
