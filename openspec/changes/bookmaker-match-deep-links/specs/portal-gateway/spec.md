## ADDED Requirements

### Requirement: Match Deep Links Enrichment and Delivery
The portal gateway controller SHALL expose `bookmakerUrls` on `GET /api/v1/matches/{id}`, combining database records, active Redis odds event URLs, and ground-truth providers (SofaScore).

#### Scenario: Client requests match details
- **WHEN** a client or browser calls `GET /api/v1/matches/{id}`
- **THEN** the response payload includes a `bookmakerUrls` object containing key-value pairs of `{bookmakerCode: eventUrl}`
- **AND** if SofaScore fixture ID is present, it includes `"sofascore": "https://www.sofascore.com/event/{id}"`
- **AND** any active odds with `eventUrl` are merged to ensure maximum coverage
