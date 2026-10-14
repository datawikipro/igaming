# Portal Gateway Specification

## Purpose
Acts as the secure API gateway and reverse proxy for web clients, extensions, and automated consumers, enforcing JWT authentication, subscription tiers, rate limits, and real-time streaming.

## Requirements

### Requirement: Subscription Tier Enforcement
The portal must enforce arbitrage yield limits and real-time streaming restrictions based on the user's authenticated subscription plan, utilizing centralized auth client verification and low-latency session caching.

#### Scenario: Anonymous and Free user access
- **WHEN** an unauthenticated guest or authenticated Free user requests surebets via `/api/v1/surebets`
- **THEN** surebets with profit margins greater than 5.0% are filtered out or masked.

#### Scenario: Premium subscriber access
- **WHEN** a user with an active Premium subscription requests surebets
- **THEN** all arbitrage opportunities (including high-yield >5% and Live events) are returned without artificial caps.

#### Scenario: Cached subscription verification
- **WHEN** repeated requests are received within the session cache TTL window
- **THEN** subscription and session checks SHALL be served from local cache without triggering redundant HTTP roundtrips to the auth microservice.

---

### Requirement: REST and WebSocket Feeds
The portal must expose secure endpoints for Surebets, Valuebets, Middles, match odds cross-tables, and virtual demo bankroll management.

#### Scenario: Public API feed query
- **WHEN** an authorized client supplies a valid `X-API-Key` to `/api/v1/feed/surebets`
- **THEN** the latest verified arbitrage stream is delivered with high-throughput JSON serialization.

### Requirement: Unified Request Token Resolution
The portal gateway MUST provide unified extraction of credentials and session context across all HTTP endpoints supporting Bearer tokens, cookies, and internal bot identities.

#### Scenario: Bearer token in Authorization header
- **WHEN** an HTTP request contains an `Authorization: Bearer <token>` header
- **THEN** the token SHALL be extracted, validated, and mapped to the user session context.

#### Scenario: Access token cookie fallback
- **WHEN** an HTTP request does not contain an `Authorization` header but provides an `access_token` cookie
- **THEN** the token SHALL be extracted from the cookie and resolved to the user session context.

#### Scenario: Internal bot identity
- **WHEN** an internal service request arrives with an `X-Bot-User-Id` header
- **THEN** the numeric user identity SHALL be resolved directly without token decoding.
