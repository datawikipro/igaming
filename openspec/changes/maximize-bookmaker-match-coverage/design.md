# Design: Maximize Bookmaker Match and Odds Coverage

## Architectural Context

The system architecture aggregates sports lines across 25+ bookmakers using a two-tier microservice architecture for each bookmaker:
1. `igaming-source-<bookmaker>-crawler`: Discovers leagues, sports categories, live/prematch match trees, and pushes light match cards into Kafka / PostgreSQL (`match_cache`).
2. `igaming-source-<bookmaker>-loader`: Loads detailed odds cards via `SELECT FOR UPDATE SKIP LOCKED` on `match_cache`, enriches full market lines, and streams normalized odds updates to `igaming-aggregator-ingestion` / Kafka topic `odds.updates`.

## Root Cause Analysis & Technical Decisions

### 1. Legacy Container Entrypoint Overrides
- **Problem**: When Java microservices are packaged via Maven Jib (`jib-maven-plugin`), classes and dependencies are laid out exploded under `/app/resources`, `/app/classes`, and `/app/libs/*`. A shell wrapper with `exec java -jar app.jar` fails immediately because no `app.jar` exists at the root.
- **Solution**: Strip all `command: ["/bin/sh", "-c"]` and `args` from crawler and loader container definitions across all YAML manifests in `igaming-k8s/`, allowing Jib's default entrypoint to execute.

### 2. Probes and Health Check Harmonization
- **Problem**: Inconsistent probes targeting `/api/v1/monitoring/info` or unready ports trigger Kubelet startup killing.
- **Solution**: Standardize on Spring Boot Actuator endpoints `/actuator/health/readiness` and `/actuator/health/liveness` with initialDelay=15s-30s, period=10s-15s, timeout=5s.

### 3. Bookmaker-Specific Remediation

#### Betboom (`igaming-source-betboom`)
- Re-apply clean Jib deployment configuration.
- Crawler polls `BETBOOM_SPORTS_URL` (`/api/site_api/v1/sporthub/tree/get_tree`) and detailed match info (`BETBOOM_EVENTS_URL`).
- Match loader connects via `APP_AGGREGATOR_URL` to ingest full lines.

#### Pinnacle (`igaming-source-pinnacle`)
- Verify Guest API key and proxy pool routing.
- Ensure crawler fetches sports catalog (`/v2/sports`) and snapshot fixtures (`/v1/fixtures/settled` & `/v1/fixtures`).

#### Olimpbet & Marathonbet (`olimpbet.ru`, `olimpbet.kz`, `marathonbet.*`)
- Configure non-blocking Hikari settings.
- Ensure loader properly uses proxy rotate failover on 403 / 429 errors.

#### Zenit (`zenit.win`)
- Expand prematch printer endpoint fetching across all sport disciplines (Soccer, Basketball, Tennis, Hockey, Volleyball, etc.).

## Verification Strategy & DoD
- Kubernetes pods in `igaming-source` namespace achieve `Running 1/1` (or `2/2` with redis sidecar).
- Total match count in aggregator `match_record` exceeds 9,000+.
- Multi-bookmaker match overlaps scale to 60+ overlaps.
- Definition of Done: 5 consecutive minutes of error-free operation monitored via `schedule` timer.
