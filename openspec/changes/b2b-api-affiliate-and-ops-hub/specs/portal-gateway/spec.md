## ADDED Requirements

### Requirement: Decoupled B2B Odds API Gateway
High-volume external queries for raw odds, value bets, and arbitrage alerts SHALL be serviced by a dedicated `igaming-b2b-api` microservice isolated from consumer-facing portal traffic.

#### Scenario: B2B odds request with quota enforcement
- **WHEN** an external B2B client sends `GET /api/v1/odds/surebets` with `X-API-Key`
- **THEN** the request is rate-limited, quota-deducted, and answered with filtered surebets in requested odds format (Decimal or American).

### Requirement: Affiliate CPA Engine under Cloudflare Zero Trust
Affiliate tracking and administrative management SHALL be isolated in `igaming-affiliate-service` with Zero Trust edge protection.

#### Scenario: Protected affiliate admin access
- **WHEN** an administrative request arrives at `/api/affiliate/admin/**`
- **THEN** the request is permitted only if the `Cf-Access-Authenticated-User-Email` header is present and validated.

#### Scenario: Public 302 click redirect and postback
- **WHEN** a user clicks `/r/{code}`
- **THEN** a unique click ID is generated, user IP is hashed with SHA-256, and user is redirected (302) to the bookmaker with `{click_id}` substituted.