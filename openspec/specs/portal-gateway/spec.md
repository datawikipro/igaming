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

---

### Requirement: REST and WebSocket Feeds
The portal must expose secure endpoints for Surebets, Valuebets, Middles, match odds cross-tables, and virtual demo bankroll management.

#### Scenario: Public API feed query
- **WHEN** an authorized client supplies a valid `X-API-Key` to `/api/v1/feed/surebets`
- **THEN** the latest verified arbitrage stream is delivered with high-throughput JSON serialization.

---

### Requirement: Ground Truth Virtual Bet Resolution
The portal gateway MUST resolve pending virtual bets against authoritative match results obtained from ground truth sports data providers (`igaming-capture-sofascore` and `igaming-capture-liveresult`), replacing random number generation.

#### Scenario: Periodic batch settlement of pending bets
- **WHEN** the settlement scheduler executes
- **THEN** it retrieves all bets with status `PENDING` whose events have started or concluded, queries the authoritative match result provider, and resolves bet outcomes (`WON`, `LOST`, `VOID`) based on official match scores.

#### Scenario: Unfinished match retention
- **WHEN** a match is still in progress or official final scores are not yet published by capture providers
- **THEN** the virtual bets remain in `PENDING` state and are scheduled for re-check in the subsequent polling cycle.

---

### Requirement: Betting Hypothesis Management API
The portal gateway SHALL expose authenticated REST endpoints allowing users and syndicates to create, list, pause, resume, reset, and inspect performance metrics for betting hypotheses.

#### Scenario: List active user hypotheses
- **WHEN** an authenticated user sends `GET /api/v1/hypotheses`
- **THEN** the portal returns all hypotheses owned by the user, including total bets, win rate, ROI, current balance, and maximum drawdown.

#### Scenario: Update hypothesis status
- **WHEN** an authorized user sends `POST /api/v1/hypotheses/{id}/toggle`
- **THEN** the hypothesis state transitions between `ACTIVE` and `PAUSED`, instantly altering forward-testing execution.

---

### Requirement: Lineup Steam Reversal Alert Feed
The portal gateway MUST detect and serve real-time Lineup Steam Reversal alerts when official starting lineups diverge significantly from baseline expectations and retail bookmakers lag behind sharp market odds adjustments.

#### Scenario: Query active steam reversal alerts
- **WHEN** an authenticated client sends `GET /api/v1/alerts/steam-reversals`
- **THEN** the portal returns matches where confirmed lineups have triggered steam moves, listing lagging bookmakers offering stale high odds, the catalyst summary, and the recommended resilient market.

#### Scenario: Subscription tier gating
- **WHEN** an unauthenticated guest or Free subscriber requests steam reversal alerts
- **THEN** high-priority steam signals are masked or limited to 1 sample, while Premium subscribers receive unconstrained real-time feeds with complete trading plans.

#### Scenario: Match-level steam reversal inspection
- **WHEN** a client sends `GET /api/v1/matches/{id}/steam-reversal`
- **THEN** the portal returns detailed tactical trading plan metadata, including entry window, expected in-play market pivot, and green-book hedging parameters.

