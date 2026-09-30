# Portal Gateway Specification

## Purpose
Acts as the secure API gateway and reverse proxy for web clients, extensions, and automated consumers, enforcing JWT authentication, subscription tiers, rate limits, and real-time streaming.

## Requirements

### Requirement: Subscription Tier Enforcement
The portal must enforce arbitrage yield limits and real-time streaming restrictions based on the user's authenticated subscription plan.

#### Scenario: Anonymous and Free user access
- **WHEN** an unauthenticated guest or authenticated Free user requests surebets via `/api/v1/surebets`
- **THEN** surebets with profit margins greater than 5.0% are filtered out or masked.

#### Scenario: Premium subscriber access
- **WHEN** a user with an active Premium subscription requests surebets
- **THEN** all arbitrage opportunities (including high-yield >5% and Live events) are returned without artificial caps.

---

### Requirement: REST and WebSocket Feeds
The portal must expose secure endpoints for Surebets, Valuebets, Middles, match odds cross-tables, and virtual demo bankroll management.

#### Scenario: Public API feed query
- **WHEN** an authorized client supplies a valid `X-API-Key` to `/api/v1/feed/surebets`
- **THEN** the latest verified arbitrage stream is delivered with high-throughput JSON serialization.
