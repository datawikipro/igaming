## ADDED Requirements

### Requirement: Affiliate Referral Routing and CPA Attribution
The portal gateway SHALL generate trackable deep links and affiliate redirect endpoints for external bookmakers to attribute user traffic for CPA and RevShare partnerships.

#### Scenario: Redirecting user to bet slip with affiliate tag
- **WHEN** a user clicks the bet placement action for a specific bookmaker leg
- **THEN** the gateway redirects through `/api/v1/affiliate/redirect` with the configured SubID and logs the outbound click.

---

### Requirement: B2B Commercial Data Feeds API
The portal gateway SHALL support commercial API key provisioning for B2B data consumers with tiered rate limiting (free, developer, commercial websocket).

#### Scenario: Consuming B2B odds stream with rate limit
- **WHEN** a B2B partner requests odds updates using a provisioned commercial API key
- **THEN** the gateway validates usage quotas and returns the aggregated real-time odds stream.
