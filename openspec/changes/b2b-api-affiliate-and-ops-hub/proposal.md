## Why

As SmartBet.guru scales to 52+ bookmakers and over 150,000 active sports fixtures, decoupling critical business boundaries becomes mandatory:
1. **Resource Isolation for B2B Odds Feed**: External syndicates and institutional partners querying raw odds and surebets at high volume must not compete with consumer web traffic on `igaming-portal`. A dedicated `igaming-b2b-api` microservice protects user response times while enforcing per-key rate limits and daily request quotas.
2. **Dedicated CPA / Affiliate Engine under Cloudflare Zero Trust**: Managing bookmaker partnerships, tracking redirect click IDs, and accepting postback webhooks from bookmakers requires a secure, standalone service (`igaming-affiliate-service`) with an isolated PostgreSQL database. The administrative surface must be completely locked down via Cloudflare Access headers (`Cf-Access-Authenticated-User-Email`).
3. **Operational Visibility (Bookmaker Ops Dashboard)**: Operations engineers and administrators need real-time visibility into the health of all 52+ bookmaker crawlers, monitoring compliance with Golden Rule #8 (line filling threshold $\ge 500$ matches), crawler staleness/delays, sports distribution, top leagues, and proxy routing channel status.

## What Changes

- **Dedicated `igaming-b2b-api` Microservice**:
  - Independent Spring Boot 3.4.1 service with OCI container build via Jib.
  - Endpoints: `GET /api/v1/odds/surebets`, `GET /api/v1/odds/valuebets`, `GET /api/v1/quota`.
  - Rate limiting with in-memory token bucket and carrier-auth API key verification.
  - Formats: Decimal and American odds.
  - Deployed in K8s via `igaming-k8s/b2b-api.yaml` on port 8080 with DNS resolution to `igaming-aggregator`.

- **Dedicated `igaming-affiliate-service` Microservice**:
  - Independent Spring Boot 3.4.1 service with dedicated PostgreSQL (`igaming-affiliate-db` StatefulSet).
  - Domain entities: `AffiliatePartner`, `AffiliateOffer`, `AffiliateClick`, `AffiliateConversion`.
  - Public Gateway: 302 redirect tracking endpoint `/r/{code}` with SHA-256 IP hashing and `{click_id}` placeholder replacement.
  - Postback Webhook: `/api/affiliate/postback/{bookmaker}` to record registrations, FTDs, and commissions.
  - Cloudflare Zero Trust security interceptor for `/api/affiliate/admin/**`.
  - Deployed in K8s via `igaming-k8s/affiliate-service.yaml`.

- **Bookmaker Fleet Ops Dashboard & Service**:
  - In `igaming-dto`: `BookmakerFleetStatsDto`, `BookmakerLeagueStatDto`, `FleetOverviewDto`.
  - In `aggregator-domain`: `OddsActualRepository` sports & top leagues projections; `BookmakerFleetService` registry of 52+ bookmakers with 30s Redis caching.
  - In `aggregator-api`: `GET /api/bookmakers/fleet` and `POST /api/bookmakers/fleet/refresh`.
  - In `igaming-portal`: Proxied endpoints `/api/v1/admin/bookmakers/fleet` and `/api/v1/admin/bookmakers/fleet/refresh`.
  - In `smartbet.guru`: `BookmakerOpsDashboard` component and `/[locale]/admin/bookmakers` page with interactive KPI counters, Golden Rule #8 progress bars, filter pills, and expandable details.

## Capabilities

### New Capabilities
- `b2b-odds-api`: Dedicated high-throughput odds and surebet API gateway with per-key quota enforcement and format transformations.
- `affiliate-cpa-engine`: Dedicated affiliate partner lifecycle management, redirect tracking, postback attribution, and Zero Trust admin protection.
- `bookmaker-ops-hub`: Real-time fleet health tracking, Golden Rule #8 line threshold compliance ($\ge 500$ matches), crawler delay telemetry, and sports/league breakdown.

### Modified Capabilities
- `portal-gateway`: Offloaded B2B endpoints to dedicated service; added proxy routes for Bookmaker Fleet Ops.
- `aggregator-core`: Added real-time fleet projection queries and Redis-cached fleet aggregation.

## Impact

- **Affected Services**: `igaming-b2b-api` (new), `igaming-affiliate-service` (new), `igaming-portal`, `aggregator-api`, `aggregator-domain`, `igaming-dto`, `smartbet.guru` (frontend).
- **Kubernetes Infrastructure**: 2 new deployments (`igaming-b2b-api`, `igaming-affiliate-service`) and 1 new PostgreSQL StatefulSet (`igaming-affiliate-db`).
- **Operational Readiness**: Instant visual detection of degraded or under-threshold bookmakers with zero impact on user-facing portal performance.